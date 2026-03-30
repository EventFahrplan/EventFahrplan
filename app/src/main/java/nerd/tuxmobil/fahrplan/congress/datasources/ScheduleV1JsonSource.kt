package nerd.tuxmobil.fahrplan.congress.datasources

import info.metadude.kotlin.library.schedule.repositories.ScheduleRepository
import info.metadude.kotlin.library.schedule.repositories.models.GetScheduleV1State.Error
import info.metadude.kotlin.library.schedule.repositories.models.GetScheduleV1State.Failure
import info.metadude.kotlin.library.schedule.repositories.models.GetScheduleV1State.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1.toSessionsNetworkModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAppHttpStatusFromResponseCode
import nerd.tuxmobil.fahrplan.congress.dataconverters.toHttpStatus
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus.HTTP_OK
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.NetworkScope
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import java.util.concurrent.ConcurrentHashMap
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

class ScheduleV1JsonSource(
    private val scope: NetworkScope,
    private val scheduleV1Repository: ScheduleRepository,
    private val scheduleSourceRepository: ScheduleSourceRepository,
) : ScheduleSource {

    private val parentJobs = ConcurrentHashMap<String, Job>()

    private val mutableLoadScheduleState = MutableSharedFlow<LoadScheduleState>(
        replay = 1,
        onBufferOverflow = DROP_OLDEST,
    )
    override val loadScheduleState: Flow<LoadScheduleState> = mutableLoadScheduleState

    // TODO Remove zombie callbacks when cleaning up UpdateService
    override fun loadSchedule(
        meta: MetaAppModel,
        isUserRequest: Boolean,
        onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
        onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
        onLoadShifts: () -> Unit,
    ) {
        val url = scheduleSourceRepository.readScheduleUrl()
        val oldMeta = meta.toMetaNetworkModel()
        val requestHeader = oldMeta.httpHeader
        val fetchingStatus = if (oldMeta.numDays == 0) InitialFetching else Fetching
        mutableLoadScheduleState.tryEmit(fetchingStatus)
        val requestIdentifier = "loadSchedule"
        parentJobs.remove(requestIdentifier)?.cancel()
        val job = scope.launchNamed(requestIdentifier) {

            suspend fun notifyFetchingDone(fetchScheduleResult: FetchScheduleResult) {
                scope.withUiContext {
                    onFetchingDone(fetchScheduleResult)
                }
            }

            suspend fun notifyParsingDone(parseScheduleResult: ParseScheduleResult) {
                scope.withUiContext {
                    onParsingDone(parseScheduleResult)
                }
            }

            suspend fun onSuccess(state: Success) {
                scheduleSourceRepository.updateScheduleLastFetchedAt()
                val newMeta = state.scheduleV1.toMetaNetworkModel(
                    responseETag = state.responseETag,
                    responseLastModifiedAt = state.responseLastModifiedAt,
                ).validate()
                scheduleSourceRepository.updateMeta(newMeta.toMetaDatabaseModel())
                scheduleSourceRepository.updateSessions(state.scheduleV1.toSessionsNetworkModel())
                mutableLoadScheduleState.tryEmit(FetchSuccess)
                notifyFetchingDone(FetchScheduleResult(HTTP_OK, url))
                mutableLoadScheduleState.tryEmit(ParseSuccess)
                val parseResult = ParseScheduleResult(isSuccess = true, state.scheduleV1.schedule.version)
                notifyParsingDone(parseResult)
                onLoadShifts()
            }

            suspend fun onError(state: Error) {
                val appHttpStatus = state.httpStatusCode.toAppHttpStatusFromResponseCode()
                val fetchResult = FetchScheduleResult(httpStatus = appHttpStatus, hostName = url, exceptionMessage = state.errorMessage)
                val fetchResultStatus = FetchFailure(httpStatus = appHttpStatus, hostName = url, exceptionMessage = state.errorMessage, isUserRequest = isUserRequest)
                mutableLoadScheduleState.tryEmit(fetchResultStatus)
                notifyFetchingDone(fetchResult)
                if (fetchResult.isNotModified || fetchResult.isSuccessful) {
                    scheduleSourceRepository.updateScheduleLastFetchedAt()
                }
                if (fetchResult.isNotModified) {
                    onLoadShifts()
                } else if (!fetchResult.isSuccessful) {
                    loadingFailed(requestIdentifier)
                }
            }

            suspend fun onFailure(state: Failure) {
                val throwable = state.throwable
                throwable.printStackTrace()
                mutableLoadScheduleState.tryEmit(throwable.toFetchFailure(url, isUserRequest))
                notifyFetchingDone(throwable.toFetchScheduleResult(url))
                loadingFailed(requestIdentifier)
            }

            scheduleV1Repository.getScheduleV1State(
                url = url,
                requestETag = requestHeader.eTag,
                lastModifiedAt = requestHeader.lastModified,
            ).collectLatest { state ->
                when (state) {
                    is Success -> onSuccess(state)
                    is Error -> onError(state)
                    is Failure -> onFailure(state)
                }
            }
        }
        parentJobs[requestIdentifier] = job
        job.invokeOnCompletion {
            parentJobs.remove(requestIdentifier, job)
        }
    }

    override fun cancelLoading() {
        val jobs = parentJobs.values.toList()
        parentJobs.clear()
        jobs.forEach(Job::cancel)
    }

    private fun loadingFailed(@Suppress("SameParameterValue") requestIdentifier: String) {
        parentJobs.remove(requestIdentifier)?.cancel()
    }

}

private fun Throwable.toFetchFailure(hostName: String, isUserRequest: Boolean) = FetchFailure(
    httpStatus = toHttpStatus(),
    hostName = hostName,
    exceptionMessage = message.orEmpty(),
    isUserRequest = isUserRequest,
)

private fun Throwable.toFetchScheduleResult(hostName: String) = FetchScheduleResult(
    httpStatus = toHttpStatus(),
    hostName = hostName,
    exceptionMessage = message.orEmpty(),
)
