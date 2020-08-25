package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys

class SharedPreferencesRepository(val context: Context) {

    private companion object {

        const val CHANGES_SEEN_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.CHANGES_SEEN"
        const val SCHEDULE_LAST_FETCHED_AT_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_LAST_FETCHED_AT"

    }

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    init {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, false)
    }

    fun getAlarmTimeIndex(): Int {
        val key = context.getString(R.string.preference_key_alarm_time_index)
        val defaultValue = context.getString(R.string.preference_default_value_alarm_time_value)
        val value = preferences.getString(key, defaultValue)!!
        val entryValues = context.resources.getStringArray(R.array.preference_entry_values_alarm_time)
        val defaultIndex = context.resources.getInteger(R.integer.preference_default_value_alarm_time_index)
        val index = entryValues.indexOf(value)
        return if (index == -1) defaultIndex else index
    }

    fun getAlarmTone(): String {
        val key = context.getString(R.string.preference_key_alarm_tone)
        val defaultValue = context.getString(R.string.preference_default_value_alarm_tone)
        return preferences.getString(key, defaultValue)!!
    }

    fun isAlternativeHighlightingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_alternative_highlighting_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_alternative_highlighting_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    fun isAutoUpdateEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_auto_update_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_auto_update_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    fun getDisplayDayIndex() = preferences.getInt(BundleKeys.PREFS_DISPLAY_DAY_INDEX, 1)

    fun setDisplayDayIndex(displayDayIndex: Int) = preferences.edit {
        putInt(BundleKeys.PREFS_DISPLAY_DAY_INDEX, displayDayIndex)
    }

    fun isInsistentAlarmsEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_insistent_alarms_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_insistent_alarms_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    fun getScheduleLastFetchedAt() =
            preferences.getLong(SCHEDULE_LAST_FETCHED_AT_KEY, 0)

    fun setScheduleLastFetchedAt(fetchedAt: Long) = preferences.edit {
        putLong(SCHEDULE_LAST_FETCHED_AT_KEY, fetchedAt)
    }

    fun getChangesSeen() =
            preferences.getBoolean(CHANGES_SEEN_KEY, true)

    fun setChangesSeen(changesSeen: Boolean) = preferences.edit {
        putBoolean(CHANGES_SEEN_KEY, changesSeen)
    }

    fun getScheduleUrl(): String {
        val defaultScheduleUrl = context.getString(R.string.preference_schedule_url_default_value)
        return preferences.getString(BundleKeys.PREFS_SCHEDULE_URL, defaultScheduleUrl)!!
    }

    fun setScheduleUrl(url: String) = preferences.edit {
        putString(BundleKeys.PREFS_SCHEDULE_URL, url)
    }

    fun getEngelsystemShiftsUrl(): String {
        val defaultShiftsUrl = context.getString(R.string.preference_engelsystem_json_export_url_default_value)
        return preferences.getString(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_URL, defaultShiftsUrl)!!
    }

    fun setEngelsystemShiftsUrl(url: String) = preferences.edit {
        putString(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_URL, url)
    }

    fun getLastEngelsystemShiftsHash() =
            preferences.getInt(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_HASH, 0)

    fun setLastEngelsystemShiftsHash(hash: Int) = preferences.edit {
        putInt(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_HASH, hash)
    }

}
