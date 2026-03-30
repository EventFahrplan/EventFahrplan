package nerd.tuxmobil.fahrplan.congress.datasources

import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAppFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import okhttp3.OkHttpClient
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

class ScheduleV1XmlSource(
    private val okHttpClient: OkHttpClient,
    private val scheduleNetworkRepository: ScheduleNetworkRepository,
    private val scheduleSourceRepository: ScheduleSourceRepository,
) : ScheduleSource {

    private val mutableLoadScheduleState = MutableSharedFlow<LoadScheduleState>(
        replay = 1,
        onBufferOverflow = DROP_OLDEST,
    )
    override val loadScheduleState: Flow<LoadScheduleState> = mutableLoadScheduleState

    /**
     * Loads the schedule from the configured url. Automated calls to this function must set the
     * [isUserRequest] parameter to `false` while call originating from a direct user interaction
     * must set the parameter to `true`.
     */
    // TODO Remove zombie callbacks when cleaning up UpdateService
    override fun loadSchedule(
        meta: MetaAppModel,
        isUserRequest: Boolean,
        onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
        onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
        onLoadShifts: () -> Unit,
    ) {
        // Fetching
        val url = scheduleSourceRepository.readScheduleUrl()
        val oldMeta = meta.toMetaNetworkModel()
        val fetchingStatus = if (oldMeta.numDays == 0) InitialFetching else Fetching
        mutableLoadScheduleState.tryEmit(fetchingStatus)
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, oldMeta.httpHeader) { fetchScheduleResult ->
            val fetchResult = fetchScheduleResult.toAppFetchScheduleResult()
            val fetchResultStatus = if (fetchResult.isSuccessful) {
                FetchSuccess
            } else {
                FetchFailure(fetchResult.httpStatus, fetchResult.hostName, fetchResult.exceptionMessage, isUserRequest)
            }
            mutableLoadScheduleState.tryEmit(fetchResultStatus)
            onFetchingDone(fetchResult)

            if (fetchResult.isNotModified || fetchResult.isSuccessful) {
                scheduleSourceRepository.updateScheduleLastFetchedAt()
            }

            if (fetchResult.isSuccessful) {
                val validMeta = oldMeta.copy(httpHeader = fetchScheduleResult.httpHeader).validate()
                scheduleSourceRepository.updateMeta(validMeta.toMetaDatabaseModel())
                // Parsing
                val parsingStatus = if (oldMeta.numDays == 0) InitialParsing else Parsing
                mutableLoadScheduleState.tryEmit(parsingStatus)
                parseSchedule(
                    scheduleXml = fetchScheduleResult.scheduleXml,
                    httpHeader = fetchScheduleResult.httpHeader,
                    oldMeta = oldMeta,
                    onParsingDone = onParsingDone,
                    onLoadShifts = onLoadShifts,
                )
            } else if (fetchResult.isNotModified) {
                onLoadShifts()
            }
        }
    }

    private fun parseSchedule(
        scheduleXml: String,
        httpHeader: HttpHeaderNetworkModel,
        oldMeta: MetaNetworkModel,
        onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
        onLoadShifts: () -> Unit,
    ) {
        scheduleNetworkRepository.parseSchedule(
            scheduleXml = scheduleXml,
            httpHeader = httpHeader,
            onUpdateSessions = { sessions ->
                scheduleSourceRepository.updateSessions(sessions)
            },
            onUpdateMeta = { meta ->
                val validMeta = meta.validate()
                scheduleSourceRepository.updateMeta(validMeta.toMetaDatabaseModel())
            },
            onParsingDone = { isSuccess: Boolean, version: String ->
                if (!isSuccess) {
                    scheduleSourceRepository.updateMeta(oldMeta.copy(httpHeader = HttpHeaderNetworkModel(eTag = "", lastModified = "")).toMetaDatabaseModel())
                }
                val parseResult = ParseScheduleResult(isSuccess, version)
                val parseScheduleStatus = if (isSuccess) ParseSuccess else ParseFailure(parseResult)
                mutableLoadScheduleState.tryEmit(parseScheduleStatus)
                onParsingDone(parseResult)
                onLoadShifts()
            }
        )
    }

    override fun cancelLoading() = Unit

}
