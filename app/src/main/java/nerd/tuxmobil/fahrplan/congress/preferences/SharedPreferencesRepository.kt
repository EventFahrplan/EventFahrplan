package nerd.tuxmobil.fahrplan.congress.preferences

interface SharedPreferencesRepository {

    fun getScheduleRefreshIntervalDefaultValue(): Int
    fun getScheduleRefreshInterval(): Int

    fun getAlarmTimeIndex(): Int

    fun isAutoUpdateEnabled(): Boolean

    fun getDisplayDayIndex(): Int
    fun setDisplayDayIndex(displayDayIndex: Int)

    fun isInsistentAlarmsEnabled(): Boolean

    fun getScheduleLastFetchedAt(): Long
    fun setScheduleLastFetchedAt(fetchedAt: Long)

    fun getScheduleNextFetchAt(): Long
    fun setScheduleNextFetchAt(fetchAt: Long)
    fun resetScheduleNextFetchAt()

    fun getScheduleNextFetchInterval(): Long
    fun setScheduleNextFetchInterval(interval: Long)
    fun resetScheduleNextFetchInterval()

    fun getChangesSeen(): Boolean
    fun setChangesSeen(changesSeen: Boolean)

    fun getAlternativeScheduleUrl(): String

    fun getEngelsystemShiftsUrl(): String

    fun getEngelsystemETag(): String
    fun setEngelsystemETag(eTag: String)

    fun getEngelsystemLastModified(): String
    fun setEngelsystemLastModified(lastModified: String)

    fun getLastEngelsystemShiftsHash(): Int
    fun setLastEngelsystemShiftsHash(hash: Int)

    fun getSelectedSessionId(): String
    fun setSelectedSessionId(sessionId: String): Boolean

    fun getSearchHistory(): List<String>
    fun setSearchHistory(history: List<String>)

}
