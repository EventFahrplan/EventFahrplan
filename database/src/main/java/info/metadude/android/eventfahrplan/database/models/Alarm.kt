package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID

data class Alarm(
    val id: Int = DEFAULT_VALUE_ID,
    val alarmTimeInMin: Int = ALARM_TIME_IN_MIN_DEFAULT,
    val dayIndex: Int = -1,
    val displayTime: Long = -1, // will be stored as signed integer
    val sessionId: String = "",
    val time: Moment = Moment.ofEpochMilli(-1), // will be stored as signed integer
    val timeText: String = "",
    val title: String = "",
)
