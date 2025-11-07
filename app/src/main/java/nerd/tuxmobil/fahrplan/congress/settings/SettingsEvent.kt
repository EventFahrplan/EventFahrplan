package nerd.tuxmobil.fahrplan.congress.settings

internal sealed interface SettingsEvent {
    data object ScheduleStatisticClicked : SettingsEvent
    data object DeviceTimezoneClicked : SettingsEvent
}
