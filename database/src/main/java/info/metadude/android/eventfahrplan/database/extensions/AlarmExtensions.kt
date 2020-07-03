package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.*
import info.metadude.android.eventfahrplan.database.models.Alarm

fun Alarm.toContentValues() = ContentValues().apply {
    put(ALARM_TIME_IN_MIN, alarmTimeInMin)
    put(DAY, day)
    put(DISPLAY_TIME, displayTime)
    put(SESSION_ID, sessionId)
    put(SESSION_TITLE, title)
    put(TIME, time)
    put(TIME_TEXT, timeText)
}
