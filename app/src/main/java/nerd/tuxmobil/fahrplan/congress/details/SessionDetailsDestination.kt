package nerd.tuxmobil.fahrplan.congress.details

sealed class SessionDetailsDestination(val route: String) {
    data object SessionDetails : SessionDetailsDestination("session_details")
    data object PickAlarmTime : SessionDetailsDestination("alarm_time_picker")
}
