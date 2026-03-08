package nerd.tuxmobil.fahrplan.congress.alarms

sealed interface AlarmsEffect {
    data class NavigateTo(val destination: AlarmsDestination) : AlarmsEffect
    data class NavigateToSession(val sessionId: String) : AlarmsEffect
}
