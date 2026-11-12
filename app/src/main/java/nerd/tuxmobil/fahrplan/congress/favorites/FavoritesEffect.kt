package nerd.tuxmobil.fahrplan.congress.favorites

sealed interface FavoritesEffect {
    data class NavigateTo(val destination: FavoritesDestination): FavoritesEffect
    data class NavigateToSession(val sessionId: String): FavoritesEffect
    data class ShareSimple(val formattedSessions: String) : FavoritesEffect
    data class ShareJson(val formattedSessions: String) : FavoritesEffect
}
