package nerd.tuxmobil.fahrplan.congress.changes

sealed interface SessionChangeState {
    data object Loading : SessionChangeState
    data class Success(val sessionChangeParameters: List<SessionChangeParameter>) : SessionChangeState
}
