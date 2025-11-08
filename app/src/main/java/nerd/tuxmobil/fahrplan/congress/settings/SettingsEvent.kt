package nerd.tuxmobil.fahrplan.congress.settings

internal sealed interface SettingsEvent {
    data object ScheduleStatisticClicked : SettingsEvent

    data object DeviceTimezoneClicked : SettingsEvent
    data object CustomizeNotificationsClicked : SettingsEvent

    data object AlarmTimeClicked : SettingsEvent
    data class SetAlarmTime(val alarmTime: Int) : SettingsEvent
}
