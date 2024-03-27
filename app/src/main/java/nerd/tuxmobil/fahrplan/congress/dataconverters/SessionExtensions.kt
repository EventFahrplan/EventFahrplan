@file:JvmName("SessionExtensions")

package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.Room
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.schedule.TrackBackgrounds
import org.threeten.bp.ZoneOffset
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun Session.shiftRoomIndexOnDays(dayIndices: Set<Int>) =
    if (dayIndex in dayIndices) {
        Session(this).apply { roomIndex += 1 }
    } else {
        this
    }

fun Session.toRoom() = Room(identifier = roomIdentifier, name = roomName)

fun Session.toDateInfo(): DateInfo = DateInfo(dayIndex, Moment.parseDate(dateText))

fun Session.toHighlightDatabaseModel() = HighlightDatabaseModel(
        sessionId = Integer.parseInt(sessionId),
        isHighlight = highlight
)

fun Session.toSessionDatabaseModel() = SessionDatabaseModel(
        sessionId = sessionId,
        abstractt = abstractt,
        date = dateText,
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
        changedTime = changedStartTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionDatabaseModel.toSessionAppModel(): Session {
    val session = Session(sessionId)

    session.abstractt = abstractt
    session.dateText = date
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
    session.changedStartTime = changedTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

fun SessionNetworkModel.toSessionAppModel(): Session {
    val session = Session(sessionId)

    session.abstractt = abstractt
    session.dateText = date
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
    session.changedStartTime = changedStartTime
    session.changedTitle = changedTitle
    session.changedTrack = changedTrack

    return session
}

/**
 * Rewrites certain properties of a session to make its rendering more pleasant and to reduce
 * visual clutter. This is accomplished by removing duplicate information, moving content to more
 * appropriate properties, and normalizing properties. To visually guide users, a common color
 * scheme is used for similar sessions. This is achieved by customizing related track names. Colors
 * are derived from track names, see [TrackBackgrounds].
 */
fun Session.sanitize(): Session {
    var tempTitle = title
    var tempSubtitle = subtitle
    var tempAbstract = abstractt
    var tempDescription = description
    var tempTrack = track
    var tempLanguage = language
    if (tempTitle.isEmpty() && tempSubtitle.isNotEmpty()) {
        tempTitle = tempSubtitle
        tempSubtitle = ""
    }
    if (tempTitle == tempSubtitle) {
        tempSubtitle = ""
    }
    if (tempAbstract == tempDescription) {
        tempAbstract = ""
    }
    if (createSpeakersString(speakers) == tempSubtitle) {
        tempSubtitle = ""
    }
    if (tempDescription.isEmpty()) {
        tempDescription = tempAbstract
        tempAbstract = ""
    }
    if (tempLanguage.isNotEmpty()) {
        tempLanguage = tempLanguage.lowercase()
    }
    if (("Sendezentrum-Bühne" == tempTrack || "Sendezentrum Bühne" == tempTrack || "xHain Berlin" == tempTrack) && type.isNotEmpty()) {
        tempTrack = type
    }
    if ("classics" == roomName && "Other" == type && tempTrack.isEmpty()) {
        tempTrack = "Classics"
    }
    if ("rC3 Lounge" == roomName) {
        tempTrack = "Music"
    }
    if (tempTrack.isEmpty() && type.isNotEmpty()) {
        tempTrack = type
    }
    return Session(this).apply {
        title = tempTitle
        subtitle = tempSubtitle
        abstractt = tempAbstract
        description = tempDescription
        track = tempTrack
        language = tempLanguage
    }
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
