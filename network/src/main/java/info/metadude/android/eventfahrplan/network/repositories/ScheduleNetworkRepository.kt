package info.metadude.android.eventfahrplan.network.repositories

import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.Session
import okhttp3.OkHttpClient

interface ScheduleNetworkRepository {

    fun fetchSchedule(okHttpClient: OkHttpClient,
                      url: String,
                      httpHeader: HttpHeader,
                      onFetchScheduleFinished: (fetchScheduleResult: FetchScheduleResult) -> Unit)

    fun parseSchedule(scheduleXml: String,
                      httpHeader: HttpHeader,
                      onUpdateSessions: (sessions: List<Session>) -> Unit,
                      onUpdateMeta: (meta: Meta) -> Unit,
                      onParsingDone: (isSuccess: Boolean, version: String) -> Unit)

}
