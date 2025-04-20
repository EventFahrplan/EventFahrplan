package nerd.tuxmobil.fahrplan.congress.models

data class SchedulableAlarm(
    val day: Int,
    val sessionId: String,
    val sessionTitle: String,
    val startTime: Long,
)
