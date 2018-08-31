package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns.*
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values.REC_OPT_OUT_ON
import info.metadude.android.eventfahrplan.database.models.Lecture

fun Lecture.toContentValues() = ContentValues().apply {
    put(EVENT_ID, eventId)
    put(ABSTRACT, abstractt)
    put(DAY, dayIndex)
    put(DATE, date)
    put(DATE_UTC, dateUTC)
    put(DESCR, description)
    put(DURATION, duration)
    put(LANG, language)
    put(LINKS, links)
    put(REC_LICENSE, recordingLicense)
    put(REC_OPTOUT, if (recordingOptOut) REC_OPT_OUT_ON else REC_OPT_OUT_OFF)
    put(REL_START, relativeStartTime)
    put(ROOM, room)
    put(ROOM_IDX, roomIndex)
    put(SLUG, slug)
    put(SPEAKERS, speakers)
    put(START, startTime)
    put(SUBTITLE, subtitle)
    put(TITLE, title)
    put(TRACK, track)
    put(TYPE, type)
    put(URL, url)

    put(CHANGED_DAY, changedDay)
    put(CHANGED_DURATION, changedDuration)
    put(CHANGED_IS_CANCELED, changedIsCanceled)
    put(CHANGED_IS_NEW, changedIsNew)
    put(CHANGED_LANGUAGE, changedLanguage)
    put(CHANGED_RECORDING_OPTOUT, changedRecordingOptOut)
    put(CHANGED_ROOM, changedRoom)
    put(CHANGED_SPEAKERS, changedSpeakers)
    put(CHANGED_SUBTITLE, changedSubtitle)
    put(CHANGED_TIME, changedTime)
    put(CHANGED_TITLE, changedTitle)
    put(CHANGED_TRACK, changedTrack)
}
