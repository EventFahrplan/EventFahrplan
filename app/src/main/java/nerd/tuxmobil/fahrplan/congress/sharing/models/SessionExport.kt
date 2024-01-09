package nerd.tuxmobil.fahrplan.congress.sharing.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
        var date: String? = null
) {
    constructor(session: Session) : this(
            sessionId = session.sessionId,
            title = session.title,
            subtitle = session.subtitle,
            day = session.day,
            room = session.roomName,
            slug = session.slug,
            url = session.url,
            speakers = session.speakers.joinToString(";"),
            track = session.track,
            type = session.lang,
            lang = session.abstractt,
            abstract = session.description,
            description = session.links,
            links = session.date
    )
}
