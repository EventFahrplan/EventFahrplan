package nerd.tuxmobil.fahrplan.congress.changes

sealed interface SessionChangeEffect {
    data class NavigateToSession(val sessionId: String) : SessionChangeEffect
    data object CancelScheduleUpdateNotification : SessionChangeEffect
}
