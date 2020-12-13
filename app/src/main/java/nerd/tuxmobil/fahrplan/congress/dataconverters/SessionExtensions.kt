package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

fun SessionNetworkModel.shiftRoomIndexOnDays(dayIndices: Set<Int>): SessionNetworkModel {
    if (dayIndices.contains(dayIndex)) {
        shiftRoomIndexBy(1)
    }
    return this
}

private fun SessionNetworkModel.shiftRoomIndexBy(amount: Int) {
    roomIndex += amount
}

fun SessionDatabaseModel.toDateInfo(): DateInfo = DateInfo(dayIndex, Moment.parseDate(date))

fun SessionAppModel.toHighlightDatabaseModel() = HighlightDatabaseModel(
        sessionId = Integer.parseInt(sessionId),
        isHighlight = highlight
)

fun SessionAppModel.toSessionDatabaseModel() = SessionDatabaseModel(
        sessionId = sessionId,
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
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
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

fun SessionAppModel.toSessionNetworkModel() = SessionNetworkModel(
        sessionId = sessionId,
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
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        title = title,
        track = track,
        type = type,
        url = url,

        changedDayIndex = changedDay,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoom = changedRoom,
        changedSpeakers = changedSpeakers,
        changedSubtitle = changedSubtitle,
        changedStartTime = changedTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionNetworkModel.toSessionDatabaseModel() = SessionDatabaseModel(
        sessionId = sessionId,
        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = dayIndex,
        description = description,
        duration = duration, // minutes
        hasAlarm = hasAlarm,
        language = language,
        links = links,
        isHighlight = isHighlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relativeStartTime,
        room = room,
        roomIndex = roomIndex,
        slug = slug,
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
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
        changedRoom = changedRoom,
        changedSpeakers = changedSpeakers,
        changedSubtitle = changedSubtitle,
        changedTime = changedStartTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionDatabaseModel.toSessionAppModel(): SessionAppModel {
    val session = SessionAppModel(sessionId)

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
    session.speakers = speakers
    session.startTime = startTime // minutes since day start
    session.subtitle = subtitle
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

fun SessionDatabaseModel.toSessionNetworkModel() = SessionNetworkModel(
        sessionId = sessionId,

        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = dayIndex,
        description = description,
        duration = duration, // minutes
        hasAlarm = hasAlarm,
        language = language,
        links = links,
        isHighlight = isHighlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relativeStartTime,
        room = room,
        roomIndex = roomIndex,
        slug = slug,
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        title = title,
        track = track,
        type = type,
        url = url,

        changedDayIndex = changedDay,
        changedDuration = changedDuration,
        changedIsCanceled = changedIsCanceled,
        changedIsNew = changedIsNew,
        changedLanguage = changedLanguage,
        changedRecordingOptOut = changedRecordingOptOut,
        changedRoom = changedRoom,
        changedSpeakers = changedSpeakers,
        changedSubtitle = changedSubtitle,
        changedStartTime = changedTime,
        changedTitle = changedTitle,
        changedTrack = changedTrack
)

fun SessionNetworkModel.toSessionAppModel(): SessionAppModel {
    val session = SessionAppModel(sessionId)

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
    session.speakers = speakers
    session.startTime = startTime // minutes since day start
    session.subtitle = subtitle
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

fun SessionNetworkModel.sanitize(): SessionNetworkModel {
    if (title == subtitle) {
        subtitle = ""
    }
    if (abstractt == description) {
        abstractt = ""
    }
    if (speakers == subtitle) {
        subtitle = ""
    }
    if (description.isEmpty()) {
        description = abstractt
        abstractt = ""
    }
    return this
}
