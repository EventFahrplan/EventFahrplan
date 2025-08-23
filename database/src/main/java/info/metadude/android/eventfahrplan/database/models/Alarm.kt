package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.DEFAULT_VALUE_ID

data class Alarm(
    val id: Int = DEFAULT_VALUE_ID,
    val dayIndex: Int = -1,
    val sessionId: String = "",
    val time: Moment = Moment.ofEpochMilli(-1), // will be stored as signed integer
    val title: String = "",
)
