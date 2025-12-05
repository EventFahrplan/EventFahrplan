package nerd.tuxmobil.fahrplan.congress.search

interface SearchEffect {
    data object NavigateBack : SearchEffect
    data class NavigateToSession(val sessionId: String) : SearchEffect
}
