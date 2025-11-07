package nerd.tuxmobil.fahrplan.congress.settings

internal sealed class SettingsNavigationDestination(val route: String) {
    data object SettingsList : SettingsNavigationDestination("settings")
    data object ScheduleStatistic : SettingsNavigationDestination("schedule_statistic")
    data object AlarmTime : SettingsNavigationDestination("alarm_time")
}
