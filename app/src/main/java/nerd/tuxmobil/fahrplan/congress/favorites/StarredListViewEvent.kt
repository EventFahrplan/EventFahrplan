package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface StarredListViewEvent {
    data class OnItemClick(val sessionId: String) : StarredListViewEvent
    data object OnDeleteAllWithConfirmationClick : StarredListViewEvent
    data object OnDeleteAllClick : StarredListViewEvent

    sealed interface Multiselect : StarredListViewEvent {
        data class OnItemLongClick(val sessionId: String) : Multiselect
        data object OnSelectionModeDismiss : Multiselect
        data object OnDeleteSelectedClick : Multiselect
    }
}
