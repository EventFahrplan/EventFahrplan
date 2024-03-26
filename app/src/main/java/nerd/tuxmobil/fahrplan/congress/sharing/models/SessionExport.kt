package nerd.tuxmobil.fahrplan.congress.sharing.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session

@JsonClass(generateAdapter = true)
data class SessionExport(
        @Json(name = "lecture_id") // Keep "lecture_id" key for Chaosflix export.
        var sessionId: String,
        var title: String,
        var subtitle: String = "",
        var day: Int = 0,
        var room: String? = null,
        var slug: String? = null,
        var url: String? = null,
        var speakers: String? = null,
        var track: String? = null,
        var type: String? = null,
        var lang: String? = null,
        var abstract: String,
        var description: String = "",
        var links: String? = null,
        @Json(name = "starts_at")
        var startsAt: String? = null,
) {
    constructor(session: Session) : this(
            sessionId = session.sessionId,
            title = session.title,
            subtitle = session.subtitle,
            day = session.dayIndex,
            room = session.roomName,
            slug = session.slug,
            url = session.url,
            speakers = session.speakers.joinToString(";"),
            track = session.track,
            type = session.type,
            lang = session.language,
            abstract = session.abstractt,
            description = session.description,
            links = session.links,
            startsAt = Moment.ofEpochMilli(session.dateUTC).toString()
    )
}
