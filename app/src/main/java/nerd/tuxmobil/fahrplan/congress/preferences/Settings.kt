package nerd.tuxmobil.fahrplan.congress.preferences

import android.net.Uri
import android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI

data class Settings(
    // Frontend preferences
    val isUseDeviceTimeZoneEnabled: Boolean = true,
    val isAlternativeHighlightingEnabled: Boolean = true,
    val isFastSwipingEnabled: Boolean = true,
    val alarmTone: Uri? = DEFAULT_ALARM_ALERT_URI,
    val isInsistentAlarmsEnabled: Boolean = false,
    val alarmTime: Int = 10,
    val isShowScheduleUpdateDialogEnabled: Boolean = true,

    // Backend preferences
    val scheduleRefreshInterval: Int = -1,
    val isAutoUpdateEnabled: Boolean = true,
    val alternativeScheduleUrl: String = "",
    val engelsystemShiftsUrl: String = "",
)
