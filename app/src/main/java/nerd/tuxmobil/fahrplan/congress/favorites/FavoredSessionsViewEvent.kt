package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface FavoredSessionsViewEvent {
    data class OnCheckedSessionsChange(val checkedSessionIds: Set<String>) : FavoredSessionsViewEvent
    data class OnItemClick(val sessionId: String) : FavoredSessionsViewEvent
    data object OnBackClick : FavoredSessionsViewEvent
    data class OnShareClick(val sessionIds: Set<String>) : FavoredSessionsViewEvent
    data class OnDeleteClick(val sessionIds: Set<String>) : FavoredSessionsViewEvent
    data object OnMultiSelectToggle : FavoredSessionsViewEvent
    
    // New event for toggling checked state of a single session
    data class OnCheckedStateChange(val sessionId: String) : FavoredSessionsViewEvent
}
