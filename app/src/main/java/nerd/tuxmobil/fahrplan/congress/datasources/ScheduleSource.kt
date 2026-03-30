package nerd.tuxmobil.fahrplan.congress.datasources

import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import info.metadude.kotlin.library.schedule.repositories.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.NetworkScope
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_JSON
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_XML
import okhttp3.OkHttpClient
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

interface ScheduleSource {

    val loadScheduleState: Flow<LoadScheduleState>

    fun loadSchedule(
        meta: MetaAppModel,
        isUserRequest: Boolean,
        onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
        onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
        onLoadShifts: () -> Unit,
    )

    fun cancelLoading()

    companion object {
        fun create(
            scheduleFileFormat: ScheduleFileFormat,
            networkScope: NetworkScope,
            okHttpClient: OkHttpClient,
            scheduleNetworkRepository: ScheduleNetworkRepository,
            scheduleV1Repository: ScheduleRepository,
            scheduleSourceRepository: ScheduleSourceRepository,
        ): ScheduleSource = when (scheduleFileFormat) {
            SCHEDULE_V1_XML -> ScheduleV1XmlSource(
                okHttpClient = okHttpClient,
                scheduleNetworkRepository = scheduleNetworkRepository,
                scheduleSourceRepository = scheduleSourceRepository,
            )

            SCHEDULE_V1_JSON -> ScheduleV1JsonSource(
                scope = networkScope,
                scheduleV1Repository = scheduleV1Repository,
                scheduleSourceRepository = scheduleSourceRepository,
            )
        }
    }
}
