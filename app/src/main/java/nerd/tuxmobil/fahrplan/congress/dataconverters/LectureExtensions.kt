package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Lecture as LectureDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Lecture as LectureNetworkModel

fun Lecture.toDateInfo(): DateInfo = DateInfo(day, date)

fun Lecture.toHighlightDatabaseModel() = HighlightDatabaseModel(
        eventId = Integer.parseInt(lecture_id),
        isHighlight = highlight
)

fun Lecture.toLectureDatabaseModel() = LectureDatabaseModel(
        eventId = lecture_id,
        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = day,
        description = description,
        duration = duration, // minutes
        hasAlarm = has_alarm,
        language = lang,
        links = links,
        isHighlight = highlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relStartTime,
        room = room,
        roomIndex = room_index,
        slug = slug,
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        title = title,
        track = track,
        type = type,

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

fun Lecture.toLectureNetworkModel() = LectureNetworkModel(
        eventId = lecture_id,
        abstractt = abstractt,
        date = date,
        dateUTC = dateUTC,
        dayIndex = day,
        description = description,
        duration = duration, // minutes
        hasAlarm = has_alarm,
        language = lang,
        links = links,
        isHighlight = highlight,
        recordingLicense = recordingLicense,
        recordingOptOut = recordingOptOut,
        relativeStartTime = relStartTime,
        room = room,
        roomIndex = room_index,
        slug = slug,
        speakers = speakers,
        startTime = startTime, // minutes since day start
        subtitle = subtitle,
        title = title,
        track = track,
        type = type,

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

fun LectureDatabaseModel.toLectureAppModel(): Lecture {
    val lecture = Lecture(eventId)

    lecture.abstractt = abstractt
    lecture.date = date
    lecture.dateUTC = dateUTC
    lecture.day = dayIndex
    lecture.description = description
    lecture.duration = duration // minutes
    lecture.has_alarm = hasAlarm
    lecture.lang = language
    lecture.links = links
    lecture.highlight = isHighlight
    lecture.recordingLicense = recordingLicense
    lecture.recordingOptOut = recordingOptOut
    lecture.relStartTime = relativeStartTime
    lecture.room = room
    lecture.room_index = roomIndex
    lecture.slug = slug
    lecture.speakers = speakers
    lecture.startTime = startTime // minutes since day start
    lecture.subtitle = subtitle
    lecture.title = title
    lecture.track = track
    lecture.type = type

    lecture.changedDay = changedDay
    lecture.changedDuration = changedDuration
    lecture.changedIsCanceled = changedIsCanceled
    lecture.changedIsNew = changedIsNew
    lecture.changedLanguage = changedLanguage
    lecture.changedRecordingOptOut = changedRecordingOptOut
    lecture.changedRoom = changedRoom
    lecture.changedSpeakers = changedSpeakers
    lecture.changedSubtitle = changedSubtitle
    lecture.changedTime = changedTime
    lecture.changedTitle = changedTitle
    lecture.changedTrack = changedTrack

    return lecture
}

fun LectureNetworkModel.toLectureAppModel(): Lecture {
    val lecture = Lecture(eventId)

    lecture.abstractt = abstractt
    lecture.date = date
    lecture.dateUTC = dateUTC
    lecture.day = dayIndex
    lecture.description = description
    lecture.duration = duration // minutes
    lecture.has_alarm = hasAlarm
    lecture.lang = language
    lecture.links = links
    lecture.highlight = isHighlight
    lecture.recordingLicense = recordingLicense
    lecture.recordingOptOut = recordingOptOut
    lecture.relStartTime = relativeStartTime
    lecture.room = room
    lecture.room_index = roomIndex
    lecture.slug = slug
    lecture.speakers = speakers
    lecture.startTime = startTime // minutes since day start
    lecture.subtitle = subtitle
    lecture.title = title
    lecture.track = track
    lecture.type = type

    lecture.changedDay = changedDayIndex
    lecture.changedDuration = changedDuration
    lecture.changedIsCanceled = changedIsCanceled
    lecture.changedIsNew = changedIsNew
    lecture.changedLanguage = changedLanguage
    lecture.changedRecordingOptOut = changedRecordingOptOut
    lecture.changedRoom = changedRoom
    lecture.changedSpeakers = changedSpeakers
    lecture.changedSubtitle = changedSubtitle
    lecture.changedTime = changedStartTime
    lecture.changedTitle = changedTitle
    lecture.changedTrack = changedTrack

    return lecture
}
