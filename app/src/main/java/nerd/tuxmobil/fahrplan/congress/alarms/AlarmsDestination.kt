package nerd.tuxmobil.fahrplan.congress.alarms

sealed class AlarmsDestination(val route: String) {
    data object ConfirmDeleteAll : AlarmsDestination("confirm_delete_all")
    data object AlarmsList : AlarmsDestination("alarms_list")
}
