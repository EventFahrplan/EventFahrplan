package nerd.tuxmobil.fahrplan.congress.favorites

sealed class StarredListDestination(val route: String) {
    data object ConfirmDeleteAll : StarredListDestination("confirm_delete_all")
    data object StarredList : StarredListDestination("starred_list")
}
