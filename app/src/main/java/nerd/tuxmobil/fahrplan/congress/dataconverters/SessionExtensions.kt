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
        copy(roomIndex = roomIndex + 1)
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
        dateText = dateText,
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

        changedDayIndex = changedDayIndex,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoomName = changedRoomName,
        changedSpeakers = changedSpeakers,
        changedStartTime = changedStartTime,
        changedSubtitle = changedSubtitle,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionDatabaseModel.toSessionAppModel(): Session {
    return Session(
        sessionId = sessionId,
        abstractt = abstractt,
        dateText = dateText,
        dateUTC = dateUTC,
        dayIndex = dayIndex,
        description = description,
        duration = duration, // minutes
        feedbackUrl = feedbackUrl,
        hasAlarm = hasAlarm,
        language = language,
        links = links,
        highlight = isHighlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relStartTime = relativeStartTime,
        roomName = roomName,
        roomIdentifier = roomIdentifier,
        roomIndex = roomIndex,
        slug = slug,
        speakers = createSpeakersList(speakers),
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        timeZoneOffset = timeZoneOffset?.let { ZoneOffset.ofTotalSeconds(it) }, // seconds
        title = title,
        track = track,
        type = type,
        url = url,

        changedDayIndex = changedDayIndex,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoomName = changedRoomName,
        changedSpeakers = changedSpeakers,
        changedStartTime = changedStartTime,
        changedSubtitle = changedSubtitle,
        changedTitle = changedTitle,
        changedTrack = changedTrack,
    )
}

fun SessionNetworkModel.toSessionAppModel(): Session {
    return Session(
        sessionId = sessionId,
        abstractt = abstractt,
        dateText = date,
        dateUTC = dateUTC,
        dayIndex = dayIndex,
        description = description,
        duration = duration, // minutes
        feedbackUrl = feedbackUrl,
        hasAlarm = hasAlarm,
        language = language,
        links = links,
        highlight = isHighlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relStartTime = relativeStartTime,
        roomName = roomName,
        roomIdentifier = roomGuid,
        roomIndex = roomIndex,
        slug = slug,
        speakers = createSpeakersList(speakers),
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        timeZoneOffset = timeZoneOffset?.let { ZoneOffset.ofTotalSeconds(it) }, // seconds
        title = title,
        track = track,
        type = type,
        url = url,

        changedDayIndex = changedDayIndex,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoomName = changedRoomName,
        changedSpeakers = changedSpeakers,
        changedStartTime = changedStartTime,
        changedSubtitle = changedSubtitle,
        changedTitle = changedTitle,
        changedTrack = changedTrack,
    )
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
    return this.copy(
        title = tempTitle,
        subtitle = tempSubtitle,
        abstractt = tempAbstract,
        description = tempDescription,
        track = tempTrack,
        language = tempLanguage,
    )
}

/**
 * Delimiter which is used in [FahrplanParser] to construct the speakers string.
 */
private const val SPEAKERS_DELIMITER_FOR_SPLITTING = ";"
private const val SPEAKERS_DELIMITER_FOR_JOINING = "; "

private fun createSpeakersList(speakers: String): List<String> {
    return if (speakers.isEmpty()) emptyList() else speakers.split(SPEAKERS_DELIMITER_FOR_SPLITTING).map { it.trim() }
}

private fun createSpeakersString(speakers: List<String>): String {
    return speakers.joinToString(SPEAKERS_DELIMITER_FOR_JOINING)
}
