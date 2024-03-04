@file:JvmName("SessionExtensions")

package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.Room
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.ZoneOffset
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun Session.shiftRoomIndexOnDays(dayIndices: Set<Int>): Session {
    if (dayIndex in dayIndices) {
        shiftRoomIndexBy(1)
    }
    return this
}

fun Session.toRoom() = Room(identifier = roomIdentifier, name = roomName)

fun Session.toDateInfo(): DateInfo = DateInfo(dayIndex, Moment.parseDate(date))

fun Session.toHighlightDatabaseModel() = HighlightDatabaseModel(
        sessionId = Integer.parseInt(sessionId),
        isHighlight = highlight
)

fun Session.toSessionDatabaseModel() = SessionDatabaseModel(
        sessionId = sessionId,
        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = dayIndex,
        description = description,
        duration = duration, // minutes
        feedbackUrl = feedbackUrl,
        hasAlarm = hasAlarm,
        language = language,
        links = links,
        isHighlight = highlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relStartTime,
        roomName = roomName,
        roomIdentifier = roomIdentifier,
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

        changedDay = changedDayIndex,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoomName = changedRoomName,
        changedSpeakers = changedSpeakers,
        changedSubtitle = changedSubtitle,
        changedTime = changedTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionDatabaseModel.toSessionAppModel(): Session {
    val session = Session(sessionId)

    session.abstractt = abstractt
    session.date = date
    session.dateUTC = dateUTC
    session.dayIndex = dayIndex
    session.description = description
    session.duration = duration // minutes
    session.feedbackUrl = feedbackUrl
    session.hasAlarm = hasAlarm
    session.language = language
    session.links = links
    session.highlight = isHighlight
    session.recordingLicense = recordingLicense
    session.recordingOptOut = recordingOptOut
    session.relStartTime = relativeStartTime
    session.roomName = roomName
    session.roomIdentifier = roomIdentifier
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

    session.changedDayIndex = changedDay
    session.changedDuration = changedDuration
    session.changedIsCanceled = changedIsCanceled
    session.changedIsNew = changedIsNew
    session.changedLanguage = changedLanguage
    session.changedRecordingOptOut = changedRecordingOptOut
    session.changedRoomName = changedRoomName
    session.changedSpeakers = changedSpeakers
    session.changedSubtitle = changedSubtitle
    session.changedTime = changedTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

fun SessionNetworkModel.toSessionAppModel(): Session {
    val session = Session(sessionId)

    session.abstractt = abstractt
    session.date = date
    session.dateUTC = dateUTC
    session.dayIndex = dayIndex
    session.description = description
    session.duration = duration // minutes
    session.feedbackUrl = feedbackUrl
    session.hasAlarm = hasAlarm
    session.language = language
    session.links = links
    session.highlight = isHighlight
    session.recordingLicense = recordingLicense
    session.recordingOptOut = recordingOptOut
    session.relStartTime = relativeStartTime
    session.roomName = roomName
    session.roomIdentifier = roomGuid
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

    session.changedDayIndex = changedDayIndex
    session.changedDuration = changedDuration
    session.changedIsCanceled = changedIsCanceled
    session.changedIsNew = changedIsNew
    session.changedLanguage = changedLanguage
    session.changedRecordingOptOut = changedRecordingOptOut
    session.changedRoomName = changedRoomName
    session.changedSpeakers = changedSpeakers
    session.changedSubtitle = changedSubtitle
    session.changedTime = changedStartTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

fun Session.sanitize(): Session {
    if (title.isEmpty() && subtitle.isNotEmpty()) {
        title = subtitle
        subtitle = ""
    }
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
    if (!language.isNullOrEmpty()) {
        language = language.lowercase()
    }
    if (("Sendezentrum-Bühne" == track || "Sendezentrum Bühne" == track || "xHain Berlin" == track) && !type.isNullOrEmpty()) {
        track = type
    }
    if ("classics" == roomName && "Other" == type && track.isNullOrEmpty()) {
        track = "Classics"
    }
    if ("rC3 Lounge" == roomName) {
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
