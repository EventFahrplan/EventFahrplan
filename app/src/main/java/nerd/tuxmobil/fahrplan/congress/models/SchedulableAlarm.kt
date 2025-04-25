package nerd.tuxmobil.fahrplan.congress.models

data class SchedulableAlarm(
    val dayIndex: Int,
    val sessionId: String,
    val sessionTitle: String,
    val startTime: Long,
)
