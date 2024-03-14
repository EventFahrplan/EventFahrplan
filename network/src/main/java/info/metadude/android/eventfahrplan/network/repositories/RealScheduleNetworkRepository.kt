package info.metadude.android.eventfahrplan.network.repositories

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.network.fetching.FetchFahrplan
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.Session
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser
import okhttp3.OkHttpClient

class RealScheduleNetworkRepository(

    logging: Logging,

) : ScheduleNetworkRepository {

    private val fetcher = FetchFahrplan(logging)
    private val parser = FahrplanParser(logging)

    override fun fetchSchedule(okHttpClient: OkHttpClient,
                               url: String,
                               httpHeader: HttpHeader,
                               onFetchScheduleFinished: (fetchScheduleResult: FetchScheduleResult) -> Unit) {
        fetcher.setListener(onFetchScheduleFinished::invoke)
        fetcher.fetch(okHttpClient, url, httpHeader)
    }

    override fun parseSchedule(scheduleXml: String,
                               httpHeader: HttpHeader,
                               onUpdateSessions: (sessions: List<Session>) -> Unit,
                               onUpdateMeta: (meta: Meta) -> Unit,
                               onParsingDone: (isSuccess: Boolean, version: String) -> Unit) {
        parser.setListener(object : FahrplanParser.OnParseCompleteListener {
            override fun onUpdateSessions(sessions: List<Session>) = onUpdateSessions.invoke(sessions)
            override fun onUpdateMeta(meta: Meta) = onUpdateMeta.invoke(meta)
            override fun onParseDone(isSuccess: Boolean, version: String) = onParsingDone.invoke(isSuccess, version)
        })
        parser.parse(scheduleXml, httpHeader)
    }

}
