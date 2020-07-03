package nerd.tuxmobil.fahrplan.congress.sharing.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import nerd.tuxmobil.fahrplan.congress.models.Session

@JsonClass(generateAdapter = true)
data class SessionExport(
        @Json(name = "lecture_id")
        var lectureId: String,
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
    constructor(lecture: Session) : this(
            lecture.lectureId,
            lecture.title,
            lecture.subtitle,
            lecture.day,
            lecture.room,
            lecture.slug,
            lecture.url,
            lecture.speakers,
            lecture.track,
            lecture.lang,
            lecture.abstractt,
            lecture.description,
            lecture.links,
            lecture.date
    )
}
