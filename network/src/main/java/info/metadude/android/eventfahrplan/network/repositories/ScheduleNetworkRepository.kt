package info.metadude.android.eventfahrplan.network.repositories

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.network.fetching.FetchFahrplan
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.Session
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser
import okhttp3.OkHttpClient

class ScheduleNetworkRepository(

    logging: Logging

) {

    private val fetcher = FetchFahrplan(logging)
    private val parser = FahrplanParser(logging)

    fun fetchSchedule(okHttpClient: OkHttpClient,
                      url: String,
                      eTag: String,
                      onFetchScheduleFinished: (fetchScheduleResult: FetchScheduleResult) -> Unit) {
        fetcher.setListener(onFetchScheduleFinished::invoke)
        fetcher.fetch(okHttpClient, url, eTag)
    }

    fun parseSchedule(scheduleXml: String,
                      eTag: String,
                      onUpdateSessions: (sessions: List<Session>) -> Unit,
                      onUpdateMeta: (meta: Meta) -> Unit,
                      onParsingDone: (result: Boolean, version: String) -> Unit) {
        parser.setListener(object : FahrplanParser.OnParseCompleteListener {
            override fun onUpdateSessions(sessions: List<Session>) = onUpdateSessions.invoke(sessions)
            override fun onUpdateMeta(meta: Meta) = onUpdateMeta.invoke(meta)
            override fun onParseDone(result: Boolean, version: String) = onParsingDone.invoke(result, version)
        })
        parser.parse(scheduleXml, eTag)
    }

}
