package nerd.tuxmobil.fahrplan.congress.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import nerd.tuxmobil.fahrplan.congress.R

class RealSharedPreferencesRepository(val context: Context) : SharedPreferencesRepository {

    private companion object {

        const val CHANGES_SEEN_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.CHANGES_SEEN"
        const val DISPLAY_DAY_INDEX_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.DISPLAY_DAY_INDEX"
        const val ENGELSYSTEM_ETAG_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_ETAG"
        const val ENGELSYSTEM_LAST_MODIFIED_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_LAST_MODIFIED"
        const val ENGELSYSTEM_SHIFTS_HASH_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_SHIFTS_HASH"
        const val SCHEDULE_LAST_FETCHED_AT_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_LAST_FETCHED_AT"
        const val SCHEDULE_NEXT_FETCH_AT_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_NEXT_FETCH_AT_KEY"
        const val SCHEDULE_NEXT_FETCH_AT_DEFAULT_VALUE = 0L
        const val SCHEDULE_NEXT_FETCH_INTERVAL_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_NEXT_FETCH_INTERVAL_KEY"
        const val SCHEDULE_NEXT_FETCH_INTERVAL_DEFAULT_VALUE = -1L
        const val SELECTED_SESSION_ID_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SELECTED_SESSION_ID_KEY"
        const val SEARCH_HISTORY_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SEARCH_HISTORY_KEY"
        const val SEARCH_HISTORY_SEPARATOR = ";"

    }

    private val preferences: SharedPreferences = DefaultSettingsRepository.getDefaultSharedPreferences(context.applicationContext)

    override fun getScheduleRefreshIntervalDefaultValue(): Int {
        return context.getString(R.string.preference_default_value_schedule_refresh_interval_value).toInt()
    }

    override fun getScheduleRefreshInterval(): Int {
        val key = context.getString(R.string.preference_key_schedule_refresh_interval_index)
        val defaultValue = context.getString(R.string.preference_default_value_schedule_refresh_interval_value)
        val value = preferences.getString(key, defaultValue)!!
        return value.toInt()
    }

    override fun getAlarmTimeIndex(): Int {
        val key = context.getString(R.string.preference_key_alarm_time_index)
        val defaultValue = context.getString(R.string.preference_default_value_alarm_time_value)
        val value = preferences.getString(key, defaultValue)!!
        val entryValues = context.resources.getStringArray(R.array.preference_entry_values_alarm_time)
        val defaultIndex = context.resources.getInteger(R.integer.preference_default_value_alarm_time_index)
        val index = entryValues.indexOf(value)
        return if (index == -1) defaultIndex else index
    }

    override fun getDisplayDayIndex() = preferences.getInt(DISPLAY_DAY_INDEX_KEY, 1)

    override fun setDisplayDayIndex(displayDayIndex: Int) = preferences.edit {
        putInt(DISPLAY_DAY_INDEX_KEY, displayDayIndex)
    }

    override fun getScheduleLastFetchedAt() =
            preferences.getLong(SCHEDULE_LAST_FETCHED_AT_KEY, 0)

    override fun setScheduleLastFetchedAt(fetchedAt: Long) = preferences.edit {
        putLong(SCHEDULE_LAST_FETCHED_AT_KEY, fetchedAt)
    }

    override fun getScheduleNextFetchAt() =
            preferences.getLong(SCHEDULE_NEXT_FETCH_AT_KEY, SCHEDULE_NEXT_FETCH_AT_DEFAULT_VALUE)

    override fun setScheduleNextFetchAt(fetchAt: Long) = preferences.edit {
        putLong(SCHEDULE_NEXT_FETCH_AT_KEY, fetchAt)
    }

    override fun resetScheduleNextFetchAt() =
        setScheduleLastFetchedAt(SCHEDULE_NEXT_FETCH_AT_DEFAULT_VALUE)

    override fun getScheduleNextFetchInterval() =
        preferences.getLong(SCHEDULE_NEXT_FETCH_INTERVAL_KEY, SCHEDULE_NEXT_FETCH_INTERVAL_DEFAULT_VALUE)

    override fun setScheduleNextFetchInterval(interval: Long) = preferences.edit {
        putLong(SCHEDULE_NEXT_FETCH_INTERVAL_KEY, interval)
    }

    override fun resetScheduleNextFetchInterval() =
        setScheduleNextFetchInterval(SCHEDULE_NEXT_FETCH_INTERVAL_DEFAULT_VALUE)

    override fun getChangesSeen() =
            preferences.getBoolean(CHANGES_SEEN_KEY, true)

    override fun setChangesSeen(changesSeen: Boolean) = preferences.edit {
        putBoolean(CHANGES_SEEN_KEY, changesSeen)
    }

    override fun getEngelsystemShiftsUrl(): String {
        val key = context.getString(R.string.preference_key_engelsystem_json_export_url)
        val defaultValue = context.getString(R.string.preference_default_value_engelsystem_json_export_url)
        return preferences.getString(key, defaultValue)!!
    }

    override fun getEngelsystemETag(): String {
        return preferences.getString(ENGELSYSTEM_ETAG_KEY, "")!!
    }

    override fun setEngelsystemETag(eTag: String) = preferences.edit {
        putString(ENGELSYSTEM_ETAG_KEY, eTag)
    }

    override fun getEngelsystemLastModified(): String {
        return preferences.getString(ENGELSYSTEM_LAST_MODIFIED_KEY, "")!!
    }

    override fun setEngelsystemLastModified(lastModified: String) = preferences.edit {
        putString(ENGELSYSTEM_LAST_MODIFIED_KEY, lastModified)
    }

    override fun getLastEngelsystemShiftsHash() =
            preferences.getInt(ENGELSYSTEM_SHIFTS_HASH_KEY, 0)

    override fun setLastEngelsystemShiftsHash(hash: Int) = preferences.edit {
        putInt(ENGELSYSTEM_SHIFTS_HASH_KEY, hash)
    }

    override fun getSelectedSessionId() =
        preferences.getString(SELECTED_SESSION_ID_KEY, "")!!

    @SuppressLint("UseKtx")
    override fun setSelectedSessionId(sessionId: String): Boolean = preferences.edit()
        .putString(SELECTED_SESSION_ID_KEY, sessionId)
        .commit()

    override fun getSearchHistory(): List<String> {
        return preferences.getString(SEARCH_HISTORY_KEY, "")!!
            .split(SEARCH_HISTORY_SEPARATOR)
    }

    override fun setSearchHistory(history: List<String>) {
        preferences.edit {
            putString(SEARCH_HISTORY_KEY, history.joinToString(SEARCH_HISTORY_SEPARATOR))
        }
    }

}
