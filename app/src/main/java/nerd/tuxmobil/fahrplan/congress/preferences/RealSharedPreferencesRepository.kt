package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import nerd.tuxmobil.fahrplan.congress.R

class RealSharedPreferencesRepository(val context: Context) : SharedPreferencesRepository {

    private companion object {

        const val CHANGES_SEEN_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.CHANGES_SEEN"
        const val DISPLAY_DAY_INDEX_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.DISPLAY_DAY_INDEX"
        const val ENGELSYSTEM_SHIFTS_HASH_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.ENGELSYSTEM_SHIFTS_HASH"
        const val SCHEDULE_LAST_FETCHED_AT_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_LAST_FETCHED_AT"
        const val SELECTED_SESSION_ID_KEY = "nerd.tuxmobil.fahrplan.congress.Prefs.SELECTED_SESSION_ID_KEY"

    }

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    init {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, false)
    }

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

    override fun getAlarmTone(): String? {
        val key = context.getString(R.string.preference_key_alarm_tone)
        val defaultValue = AlarmTonePreference.DEFAULT_VALUE_STRING
        return preferences.getString(key, defaultValue)
    }

    override fun isUseDeviceTimeZoneEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_use_device_time_zone_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_use_device_time_zone_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isAlternativeHighlightingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_alternative_highlighting_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_alternative_highlighting_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isAutoUpdateEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_auto_update_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_auto_update_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    override fun getDisplayDayIndex() = preferences.getInt(DISPLAY_DAY_INDEX_KEY, 1)

    override fun setDisplayDayIndex(displayDayIndex: Int) = preferences.edit {
        putInt(DISPLAY_DAY_INDEX_KEY, displayDayIndex)
    }

    override fun isInsistentAlarmsEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_insistent_alarms_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_insistent_alarms_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    override fun getScheduleLastFetchedAt() =
            preferences.getLong(SCHEDULE_LAST_FETCHED_AT_KEY, 0)

    override fun setScheduleLastFetchedAt(fetchedAt: Long) = preferences.edit {
        putLong(SCHEDULE_LAST_FETCHED_AT_KEY, fetchedAt)
    }

    override fun getChangesSeen() =
            preferences.getBoolean(CHANGES_SEEN_KEY, true)

    override fun setChangesSeen(changesSeen: Boolean) = preferences.edit {
        putBoolean(CHANGES_SEEN_KEY, changesSeen)
    }

    override fun getAlternativeScheduleUrl(): String {
        val key = context.getString(R.string.preference_key_alternative_schedule_url)
        val defaultValue = context.getString(R.string.preference_default_value_alternative_schedule_url)
        return preferences.getString(key, defaultValue)!!
    }

    override fun getEngelsystemShiftsUrl(): String {
        val key = context.getString(R.string.preference_key_engelsystem_json_export_url)
        val defaultValue = context.getString(R.string.preference_default_value_engelsystem_json_export_url)
        return preferences.getString(key, defaultValue)!!
    }

    override fun getLastEngelsystemShiftsHash() =
            preferences.getInt(ENGELSYSTEM_SHIFTS_HASH_KEY, 0)

    override fun setLastEngelsystemShiftsHash(hash: Int) = preferences.edit {
        putInt(ENGELSYSTEM_SHIFTS_HASH_KEY, hash)
    }

    override fun getSelectedSessionId() =
        preferences.getString(SELECTED_SESSION_ID_KEY, "")!!

    override fun setSelectedSessionId(sessionId: String): Boolean = preferences.edit()
        .putString(SELECTED_SESSION_ID_KEY, sessionId)
        .commit()

    override fun isScheduleLandscape(): Boolean {
        val key = context.getString(R.string.preference_key_orientation_schedule_landscape)
        val defaultValue =  context.resources.getBoolean(R.bool.preference_default_orientation_schedule_landscape)
        return preferences.getBoolean(key, defaultValue)!!
    }

}
