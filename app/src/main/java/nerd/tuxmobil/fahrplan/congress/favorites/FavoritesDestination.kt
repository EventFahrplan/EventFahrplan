package nerd.tuxmobil.fahrplan.congress.favorites

sealed class FavoritesDestination(val route: String) {
    data object ConfirmDeleteAll : FavoritesDestination("confirm_delete_all")
    data object Favorites : FavoritesDestination("favorites")
}
