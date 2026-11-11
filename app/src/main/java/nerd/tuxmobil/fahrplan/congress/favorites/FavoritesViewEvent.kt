package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface FavoritesViewEvent {
    data class OnItemClick(val sessionId: String) : FavoritesViewEvent
    data object OnDeleteAllWithConfirmationClick : FavoritesViewEvent
    data object OnDeleteAllClick : FavoritesViewEvent
    data object OnShareClick : FavoritesViewEvent
    data object OnShareToChaosflixClick : FavoritesViewEvent

    sealed interface Multiselect : FavoritesViewEvent {
        data class OnItemLongClick(val sessionId: String) : Multiselect
        data object OnSelectionModeDismiss : Multiselect
        data object OnDeleteSelectedClick : Multiselect
    }
}
