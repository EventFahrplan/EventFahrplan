package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment

data class SchedulableAlarm(
    val dayIndex: Int,
    val sessionId: String,
    val sessionTitle: String,
    val startTime: Moment,
)
