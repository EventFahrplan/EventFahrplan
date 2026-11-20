package nerd.tuxmobil.fahrplan.congress.changes

sealed interface SessionChangeState {
    data object Loading : SessionChangeState

    /**
     * @param sessionChangeParameters The schedule changes to be shown. Filtered by
     * favorites if [favoritesFilterEnabled] is `true`.
     * @param favoritesFilterEnabled Whether only changes of favorite sessions are shown.
     * @param hasUnfilteredChanges Whether there are any schedule changes at all, regardless
     * of the filter. Used to decide whether filtering is offered.
     */
    data class Success(
        val sessionChangeParameters: List<SessionChangeParameter>,
        val favoritesFilterEnabled: Boolean,
        val hasUnfilteredChanges: Boolean,
    ) : SessionChangeState
}
