package info.metadude.android.eventfahrplan.network.repositories

import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.Session
import okhttp3.OkHttpClient

interface ScheduleNetworkRepository {

    fun fetchSchedule(okHttpClient: OkHttpClient,
                      url: String,
                      eTag: String,
                      onFetchScheduleFinished: (fetchScheduleResult: FetchScheduleResult) -> Unit)

    fun parseSchedule(scheduleXml: String,
                      eTag: String,
                      onUpdateSessions: (sessions: List<Session>) -> Unit,
                      onUpdateMeta: (meta: Meta) -> Unit,
                      onParsingDone: (result: Boolean, version: String) -> Unit)

}
