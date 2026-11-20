package nerd.tuxmobil.fahrplan.congress.changes

sealed interface SessionChangeViewEvent {
    data class OnSessionChangeItemClick(val sessionId: String) : SessionChangeViewEvent
    data object OnScheduleChangesSeen : SessionChangeViewEvent
    data class OnFavoritesFilterToggle(val enabled: Boolean) : SessionChangeViewEvent
}
