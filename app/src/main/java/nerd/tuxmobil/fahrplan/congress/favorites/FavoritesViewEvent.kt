package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface StarredListViewEvent {
    data class OnItemClick(val sessionId: String) : StarredListViewEvent
    data object OnDeleteAllWithConfirmationClick : StarredListViewEvent
    data object OnDeleteAllClick : StarredListViewEvent
    data object OnShareClick : StarredListViewEvent
    data object OnShareToChaosflixClick : StarredListViewEvent

    sealed interface Multiselect : StarredListViewEvent {
        data class OnItemLongClick(val sessionId: String) : Multiselect
        data object OnSelectionModeDismiss : Multiselect
        data object OnDeleteSelectedClick : Multiselect
    }
}
