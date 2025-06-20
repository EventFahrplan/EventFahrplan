package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract

data class Alarm(
    val id: Int,
    val alarmTimeInMin: Int,
    val dayIndex: Int,
    val displayTime: Long,
    val sessionId: String,
    val sessionTitle: String,
    val startTime: Moment,
    val timeText: String,
) {

    constructor(
        alarmTimeInMin: Int,
        dayIndex: Int,
        displayTime: Long,
        sessionId: String,
        sessionTitle: String,
        startTime: Moment,
        timeText: String,
    ) : this(
        id = DEFAULT_VALUE_ID,
        alarmTimeInMin = alarmTimeInMin,
        dayIndex = dayIndex,
        displayTime = displayTime,
        sessionId = sessionId,
        sessionTitle = sessionTitle,
        startTime = startTime,
        timeText = timeText,
    )

    companion object {
        const val DEFAULT_VALUE_ID = FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID
    }

}
