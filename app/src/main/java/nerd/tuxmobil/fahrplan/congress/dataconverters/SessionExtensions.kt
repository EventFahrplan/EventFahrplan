@file:JvmName("SessionExtensions")

package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.ZoneOffset
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun Session.shiftRoomIndexOnDays(dayIndices: Set<Int>): Session {
    if (dayIndices.contains(day)) {
        shiftRoomIndexBy(1)
    }
    return this
}

/**
 * Returns a moment based on the start time of this session.
 */
fun Session.toStartsAtMoment(): Moment {
    require(dateUTC > 0) { "Field 'dateUTC' is 0." }
    return Moment.ofEpochMilli(dateUTC)
}

fun Session.toDateInfo(): DateInfo = DateInfo(day, Moment.parseDate(date))

fun Session.toHighlightDatabaseModel() = HighlightDatabaseModel(
        sessionId = Integer.parseInt(sessionId),
        isHighlight = highlight
)

fun Session.toSessionDatabaseModel() = SessionDatabaseModel(
        sessionId = sessionId,
        guid = guid,
        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = day,
        description = description,
        duration = duration, // minutes
        hasAlarm = hasAlarm,
        language = lang,
        links = links,
        isHighlight = highlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relStartTime,
        room = room,
        roomIndex = roomIndex,
        slug = slug,
        speakers = createSpeakersString(speakers),
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        timeZoneOffset = timeZoneOffset?.totalSeconds, // seconds
        title = title,
        track = track,
        type = type,
        url = url,

        changedDay = changedDay,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoom = changedRoom,
        changedSpeakers = changedSpeakers,
        changedSubtitle = changedSubtitle,
        changedTime = changedTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionDatabaseModel.toSessionAppModel(): Session {
    val session = Session(sessionId)
    session.guid = guid

    session.abstractt = abstractt
    session.date = date
    session.dateUTC = dateUTC
    session.day = dayIndex
    session.description = description
    session.duration = duration // minutes
    session.hasAlarm = hasAlarm
    session.lang = language
    session.links = links
    session.highlight = isHighlight
    session.recordingLicense = recordingLicense
    session.recordingOptOut = recordingOptOut
    session.relStartTime = relativeStartTime
    session.room = room
    session.roomIndex = roomIndex
    session.slug = slug
    session.speakers = createSpeakersList(speakers)
    session.startTime = startTime // minutes since day start
    session.subtitle = subtitle
    session.timeZoneOffset = timeZoneOffset?.let { ZoneOffset.ofTotalSeconds(it) } // seconds
    session.title = title
    session.track = track
    session.type = type
    session.url = url

    session.changedDay = changedDay
    session.changedDuration = changedDuration
    session.changedIsCanceled = changedIsCanceled
    session.changedIsNew = changedIsNew
    session.changedLanguage = changedLanguage
    session.changedRecordingOptOut = changedRecordingOptOut
    session.changedRoom = changedRoom
    session.changedSpeakers = changedSpeakers
    session.changedSubtitle = changedSubtitle
    session.changedTime = changedTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

fun SessionNetworkModel.toSessionAppModel(): Session {
    val session = Session(sessionId)
    session.guid = guid

    session.abstractt = abstractt
    session.date = date
    session.dateUTC = dateUTC
    session.day = dayIndex
    session.description = description
    session.duration = duration // minutes
    session.hasAlarm = hasAlarm
    session.lang = language
    session.links = links
    session.highlight = isHighlight
    session.recordingLicense = recordingLicense
    session.recordingOptOut = recordingOptOut
    session.relStartTime = relativeStartTime
    session.room = room
    session.roomIndex = roomIndex
    session.slug = slug
    session.speakers = createSpeakersList(speakers)
    session.startTime = startTime // minutes since day start
    session.subtitle = subtitle
    session.timeZoneOffset = timeZoneOffset?.let { ZoneOffset.ofTotalSeconds(it) } // seconds
    session.title = title
    session.track = track
    session.type = type
    session.url = url

    session.changedDay = changedDayIndex
    session.changedDuration = changedDuration
    session.changedIsCanceled = changedIsCanceled
    session.changedIsNew = changedIsNew
    session.changedLanguage = changedLanguage
    session.changedRecordingOptOut = changedRecordingOptOut
    session.changedRoom = changedRoom
    session.changedSpeakers = changedSpeakers
    session.changedSubtitle = changedSubtitle
    session.changedTime = changedStartTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

fun Session.sanitize(): Session {
    if (title == subtitle) {
        subtitle = ""
    }
    if (abstractt == description) {
        abstractt = ""
    }
    if (createSpeakersString(speakers) == subtitle) {
        subtitle = ""
    }
    if (description.isEmpty()) {
        description = abstractt
        abstractt = ""
    }
    if (!lang.isNullOrEmpty()) {
        lang = lang.lowercase()
    }
    if (("Sendezentrum-Bühne" == track || "Sendezentrum Bühne" == track || "xHain Berlin" == track) && !type.isNullOrEmpty()) {
        track = type
    }
    if ("classics" == room && "Other" == type && track.isNullOrEmpty()) {
        track = "Classics"
    }
    if ("rC3 Lounge" == room) {
        track = "Music"
    }
    if (track.isNullOrEmpty() && !type.isNullOrEmpty()) {
        track = type
    }
    return this
}

/**
 * Delimiter which is used in [FahrplanParser] to construct the speakers string.
 */
private const val SPEAKERS_DELIMITER = ";"

private fun createSpeakersList(speakers: String): List<String> {
    return if (speakers.isEmpty()) emptyList() else speakers.split(SPEAKERS_DELIMITER)
}

private fun createSpeakersString(speakers: List<String>): String {
    return speakers.joinToString(SPEAKERS_DELIMITER)
}
