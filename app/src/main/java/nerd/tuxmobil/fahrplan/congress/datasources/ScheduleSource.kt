package nerd.tuxmobil.fahrplan.congress.datasources

import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import kotlinx.coroutines.flow.Flow
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat
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

    companion object {
        fun create(
            scheduleFileFormat: ScheduleFileFormat,
            okHttpClient: OkHttpClient,
            scheduleNetworkRepository: ScheduleNetworkRepository,
            scheduleSourceRepository: ScheduleSourceRepository,
        ): ScheduleSource = when (scheduleFileFormat) {
            SCHEDULE_V1_XML -> ScheduleV1XmlSource(
                okHttpClient = okHttpClient,
                scheduleNetworkRepository = scheduleNetworkRepository,
                scheduleSourceRepository = scheduleSourceRepository,
            )
        }
    }
}
