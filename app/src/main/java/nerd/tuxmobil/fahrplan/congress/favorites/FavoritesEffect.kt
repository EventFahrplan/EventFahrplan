package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface StarredListEffect {
    data class NavigateTo(val destination: FavoritesDestination): StarredListEffect
    data class NavigateToSession(val sessionId: String): StarredListEffect
    data class ShareSimple(val formattedSessions: String) : StarredListEffect
    data class ShareJson(val formattedSessions: String) : StarredListEffect
}
