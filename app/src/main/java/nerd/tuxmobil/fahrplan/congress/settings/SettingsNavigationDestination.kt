package nerd.tuxmobil.fahrplan.congress.settings

internal sealed class SettingsNavigationDestination(val route: String) {
    data object SettingsList : SettingsNavigationDestination("settings")
    data object ScheduleRefreshInterval : SettingsNavigationDestination("schedule_refresh_interval")
    data object ScheduleStatistic : SettingsNavigationDestination("schedule_statistic")
    data object AlternativeScheduleUrl : SettingsNavigationDestination("alternative_schedule_url")
    data object AlarmTime : SettingsNavigationDestination("alarm_time")
    data object EngelSystemUrl : SettingsNavigationDestination("engelsystem_url")
}
