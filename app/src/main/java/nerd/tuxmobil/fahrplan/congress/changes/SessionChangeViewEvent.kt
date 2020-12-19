package nerd.tuxmobil.fahrplan.congress.changes

sealed interface SessionChangeViewEvent {
    data class OnSessionChangeItemClick(val guid: String) : SessionChangeViewEvent
}
