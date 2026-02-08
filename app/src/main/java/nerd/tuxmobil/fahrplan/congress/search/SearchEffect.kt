package nerd.tuxmobil.fahrplan.congress.search

sealed interface SearchEffect {
    data object NavigateBack : SearchEffect
    data class NavigateToSession(val sessionId: String) : SearchEffect
}
