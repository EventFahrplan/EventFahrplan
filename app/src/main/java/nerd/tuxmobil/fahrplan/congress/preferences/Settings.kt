package nerd.tuxmobil.fahrplan.congress.preferences

data class Settings(
    // Frontend preferences
    val isUseDeviceTimeZoneEnabled: Boolean = true,
    val isAlternativeHighlightingEnabled: Boolean = true,
    val isFastSwipingEnabled: Boolean = true,
    val alarmTone: String? = null,
    val isInsistentAlarmsEnabled: Boolean = false,
    val alarmTime: Int = 10,

    // Backend preferences
    val scheduleRefreshInterval: Int = -1,
    val isAutoUpdateEnabled: Boolean = true,
    val alternativeScheduleUrl: String? = null,
    val engelsystemShiftsUrl: String? = null,
)
