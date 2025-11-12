package nerd.tuxmobil.fahrplan.congress.settings

import android.net.Uri

internal sealed interface SettingsEvent {
    data object ScheduleStatisticClicked : SettingsEvent

    data object AutoUpdateClicked : SettingsEvent
    data object DeviceTimezoneClicked : SettingsEvent
    data object CustomizeNotificationsClicked : SettingsEvent
    data object AlternativeScheduleUrlClicked : SettingsEvent
    data class SetAlternativeScheduleUrl(val url: String) : SettingsEvent

    data object AlarmToneClicked : SettingsEvent
    data class SetAlarmTone(val alarmTone: Uri?): SettingsEvent
    data object AlarmTimeClicked : SettingsEvent
    data class SetAlarmTime(val alarmTime: Int) : SettingsEvent

    data object EngelsystemUrlClicked : SettingsEvent
    data class SetEngelsystemShiftsUrl(val url: String) : SettingsEvent
}
