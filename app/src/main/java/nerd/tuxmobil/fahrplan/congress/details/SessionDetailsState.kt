package nerd.tuxmobil.fahrplan.congress.details

internal sealed interface SessionDetailsState {
    data object Loading : SessionDetailsState
    data class Success(
        val sessionDetailsParameter: SessionDetailsParameter,
        val toolbarActions: List<SessionDetailsToolbarAction>,
    ) : SessionDetailsState
}
