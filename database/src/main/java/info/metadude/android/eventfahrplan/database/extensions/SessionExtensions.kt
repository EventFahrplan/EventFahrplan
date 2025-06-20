package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionByNotificationIdTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ABSTRACT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DAY_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_CANCELED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_NEW
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_LANGUAGE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_RECORDING_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_ROOM_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_START_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_TEXT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_UTC
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DAY_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DESCR
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.FEEDBACK_URL
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LANG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LINKS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_LICENSE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REL_START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_IDENTIFIER
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SLUG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TIME_ZONE_OFFSET
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TYPE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.URL
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values.REC_OPT_OUT_ON
import info.metadude.android.eventfahrplan.database.models.Session

fun Session.toContentValues() = contentValuesOf(
        SESSION_ID to sessionId,
        ABSTRACT to abstractt,
        DAY_INDEX to dayIndex,
        DATE_TEXT to dateText,
        DATE_UTC to dateUTC,
        DESCR to description,
        DURATION to duration.toWholeMinutes().toInt(),
        FEEDBACK_URL to feedbackUrl,
        LANG to language,
        LINKS to links,
        REC_LICENSE to recordingLicense,
        REC_OPTOUT to if (recordingOptOut) REC_OPT_OUT_ON else REC_OPT_OUT_OFF,
        REL_START to relativeStartTime.toWholeMinutes().toInt(),
        ROOM_NAME to roomName,
        ROOM_IDENTIFIER to roomIdentifier,
        ROOM_INDEX to roomIndex,
        SLUG to slug,
        SPEAKERS to speakers,
        START to startTime.toWholeMinutes().toInt(),
        SUBTITLE to subtitle,
        TIME_ZONE_OFFSET to timeZoneOffset,
        TITLE to title,
        TRACK to track,
        TYPE to type,
        URL to url,

        CHANGED_DAY_INDEX to changedDayIndex,
        CHANGED_DURATION to changedDuration,
        CHANGED_IS_CANCELED to changedIsCanceled,
        CHANGED_IS_NEW to changedIsNew,
        CHANGED_LANGUAGE to changedLanguage,
        CHANGED_RECORDING_OPTOUT to changedRecordingOptOut,
        CHANGED_ROOM_NAME to changedRoomName,
        CHANGED_SPEAKERS to changedSpeakers,
        CHANGED_START_TIME to changedStartTime,
        CHANGED_SUBTITLE to changedSubtitle,
        CHANGED_TITLE to changedTitle,
        CHANGED_TRACK to changedTrack
)

/**
 * Converts a session ID into [ContentValues].
 */
fun String.toContentValues() = contentValuesOf(
        SessionByNotificationIdTable.Columns.SESSION_ID to this
)
