package info.metadude.android.eventfahrplan.database.extensions

import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.ALARM_TIME_IN_MIN
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DISPLAY_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.GUID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME_TEXT
import info.metadude.android.eventfahrplan.database.models.Alarm

fun Alarm.toContentValues() = contentValuesOf(
        ALARM_TIME_IN_MIN to alarmTimeInMin,
        DAY to day,
        DISPLAY_TIME to displayTime,
        GUID to guid,
        SESSION_TITLE to title,
        TIME to time,
        TIME_TEXT to timeText
)
