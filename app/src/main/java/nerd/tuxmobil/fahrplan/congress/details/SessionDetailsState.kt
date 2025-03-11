package nerd.tuxmobil.fahrplan.congress.details

sealed interface SessionDetailsState {
    data object Loading : SessionDetailsState
    data class Success(val sessionDetailsParameter: SessionDetailsParameter) : SessionDetailsState
}
