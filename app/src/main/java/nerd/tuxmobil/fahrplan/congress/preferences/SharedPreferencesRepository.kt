package nerd.tuxmobil.fahrplan.congress.preferences

interface SharedPreferencesRepository {

    fun getScheduleRefreshIntervalDefaultValue(): Int
    fun getScheduleRefreshInterval(): Int

    fun getAlarmTimeIndex(): Int
    fun getAlarmTone(): String?

    fun isUseDeviceTimeZoneEnabled(): Boolean

    fun isAlternativeHighlightingEnabled(): Boolean

    fun isAutoUpdateEnabled(): Boolean

    fun getDisplayDayIndex(): Int
    fun setDisplayDayIndex(displayDayIndex: Int)

    fun isInsistentAlarmsEnabled(): Boolean

    fun getScheduleLastFetchedAt(): Long
    fun setScheduleLastFetchedAt(fetchedAt: Long)

    fun getChangesSeen(): Boolean
    fun setChangesSeen(changesSeen: Boolean)

    fun getAlternativeScheduleUrl(): String

    fun getEngelsystemShiftsUrl(): String

    fun getLastEngelsystemShiftsHash(): Int
    fun setLastEngelsystemShiftsHash(hash: Int)

    fun getSelectedSessionId(): String
    fun setSelectedSessionId(sessionId: String): Boolean

    fun isForceLandscapeMode() : Boolean

}
