package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID

data class Alarm(
    val id: Int = DEFAULT_VALUE_ID,
    val alarmTimeInMin: Int = ALARM_TIME_IN_MIN_DEFAULT,
    val day: Int = -1,
    val displayTime: Long = -1, // will be stored as signed integer
    val sessionId: String = "",
    val time: Long = -1, // will be stored as signed integer
    val timeText: String = "",
    val title: String = "",
)
