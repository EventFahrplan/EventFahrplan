package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract

data class Alarm(
    val id: Int,
    val dayIndex: Int,
    val sessionId: String,
    val sessionTitle: String,
    val startTime: Moment,
) {

    constructor(
        dayIndex: Int,
        sessionId: String,
        sessionTitle: String,
        startTime: Moment,
    ) : this(
        id = DEFAULT_VALUE_ID,
        dayIndex = dayIndex,
        sessionId = sessionId,
        sessionTitle = sessionTitle,
        startTime = startTime,
    )

    companion object {
        const val DEFAULT_VALUE_ID = FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID
    }

}
