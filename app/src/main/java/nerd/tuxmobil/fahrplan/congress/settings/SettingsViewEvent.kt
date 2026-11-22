package nerd.tuxmobil.fahrplan.congress.settings

import android.net.Uri

internal sealed interface SettingsViewEvent {
    data object ScheduleRefreshIntervalClicked : SettingsViewEvent
    data class SetScheduleRefreshInterval(val refreshInterval: Int) : SettingsViewEvent
    data object ScheduleStatisticClicked : SettingsViewEvent

    data object AutoUpdateClicked : SettingsViewEvent
    data object DeviceTimezoneClicked : SettingsViewEvent
    data object CustomizeNotificationsClicked : SettingsViewEvent
    data object AlternativeScheduleUrlClicked : SettingsViewEvent
    data class SetAlternativeScheduleUrl(val url: String) : SettingsViewEvent
    data object AlternativeHighlightingClicked : SettingsViewEvent
    data object FastSwipingClicked : SettingsViewEvent
    data object ShowScheduleUpdateDialogClicked : SettingsViewEvent
    data object ScheduleChangesFavoritesFilterClicked : SettingsViewEvent
    data object ShowOnLockscreenClicked : SettingsViewEvent

    data object AlarmToneClicked : SettingsViewEvent
    data class SetAlarmTone(val alarmTone: Uri?): SettingsViewEvent
    data object InsistentAlarmClicked : SettingsViewEvent
    data object AlarmTimeClicked : SettingsViewEvent
    data class SetAlarmTime(val alarmTime: Int) : SettingsViewEvent

    data object SocialMediaHashtagsHandlesClicked : SettingsViewEvent
    data object ResetSocialMediaHashtagsHandles : SettingsViewEvent
    data class SetSocialMediaHashtagsHandles(val handles: String) : SettingsViewEvent

    data object EngelsystemUrlClicked : SettingsViewEvent
    data class SetEngelsystemShiftsUrl(val url: String) : SettingsViewEvent
}
