package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion

internal class DefaultSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    companion object {
        // For backwards-compatibility we copy the behavior of PreferenceManager.getDefaultSharedPreferences()
        fun getDefaultSharedPreferences(context: Context): SharedPreferences {
            val defaultSharedPreferencesName = "${context.packageName}_preferences"
            return context.getSharedPreferences(defaultSharedPreferencesName, Context.MODE_PRIVATE)
        }
    }

    private val preferences = getDefaultSharedPreferences(context.applicationContext)

    private val userPreferencesFlow = callbackFlow {
        trySendBlocking(getSettings())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySendBlocking(getSettings())
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    override val settingsStream: Flow<Settings> = userPreferencesFlow

    override fun setUseDeviceTimeZone(enable: Boolean) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_use_device_time_zone_enabled)
            putBoolean(key, enable)
        }
    }

    override fun setAlternativeHighlighting(enable: Boolean) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_alternative_highlighting_enabled)
            putBoolean(key, enable)
        }
    }

    override fun setFastSwiping(enable: Boolean) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_fast_swiping_enabled)
            putBoolean(key, enable)
        }
    }

    override fun setAlarmTone(alarmTone: Uri?) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_alarm_tone)
            val value = AlarmToneConversion.getPersistableString(alarmTone)
            putString(key, value)
        }
    }

    override fun setInsistentAlarms(enable: Boolean) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_insistent_alarms_enabled)
            putBoolean(key, enable)
        }
    }

    override fun setAlarmTime(alarmTime: Int) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_alarm_time_index)
            putString(key, alarmTime.toString())
        }
    }

    override fun setScheduleRefreshInterval(interval: Int) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_schedule_refresh_interval_index)
            putString(key, interval.toString())
        }
    }

    override fun setAutoUpdateEnabled(enable: Boolean) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_auto_update_enabled)
            putBoolean(key, enable)
        }
    }

    override fun setAlternativeScheduleUrl(url: String) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_alternative_schedule_url)
            putString(key, url)
        }
    }

    override fun setEngelsystemShiftsUrl(url: String) {
        preferences.edit {
            val key = context.getString(R.string.preference_key_engelsystem_json_export_url)
            putString(key, url)
        }
    }

    private fun getSettings(): Settings {
        return Settings(
            isUseDeviceTimeZoneEnabled = isUseDeviceTimeZoneEnabled(),
            isAlternativeHighlightingEnabled = isAlternativeHighlightingEnabled(),
            isFastSwipingEnabled = isFastSwipingEnabled(),
            alarmTone = getAlarmTone(),
            isInsistentAlarmsEnabled = isInsistentAlarmsEnabled(),
            alarmTime = getAlarmTime(),
            scheduleRefreshInterval = getScheduleRefreshInterval(),
            isAutoUpdateEnabled = isAutoUpdateEnabled(),
            alternativeScheduleUrl = getAlternativeScheduleUrl(),
            engelsystemShiftsUrl = getEngelsystemShiftsUrl(),
        )
    }

    private fun getScheduleRefreshInterval(): Int {
        val key = context.getString(R.string.preference_key_schedule_refresh_interval_index)
        val defaultValue = context.getString(R.string.preference_default_value_schedule_refresh_interval_value)
        val value = preferences.getString(key, defaultValue)!!
        return value.toInt()
    }

    private fun getAlarmTime(): Int {
        val key = context.getString(R.string.preference_key_alarm_time_index)
        val defaultValue = context.getString(R.string.preference_default_value_alarm_time_value)
        val value = preferences.getString(key, defaultValue)!!
        return value.toInt()
    }

    override fun getAlarmTone(): Uri? {
        val key = context.getString(R.string.preference_key_alarm_tone)
        val defaultValue = AlarmTonePreference.DEFAULT_VALUE_STRING
        val value = preferences.getString(key, defaultValue)!!
        return AlarmToneConversion.getPickerIntentUri(value)
    }

    private fun isUseDeviceTimeZoneEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_use_device_time_zone_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_use_device_time_zone_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun isAlternativeHighlightingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_alternative_highlighting_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_alternative_highlighting_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun isFastSwipingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_fast_swiping_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_fast_swiping_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun isAutoUpdateEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_auto_update_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_auto_update_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun isInsistentAlarmsEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_insistent_alarms_enabled)
        val defaultValue = context.resources.getBoolean(R.bool.preference_default_value_insistent_alarms_enabled)
        return preferences.getBoolean(key, defaultValue)
    }

    private fun getAlternativeScheduleUrl(): String {
        val key = context.getString(R.string.preference_key_alternative_schedule_url)
        val defaultValue = context.getString(R.string.preference_default_value_alternative_schedule_url)
        return preferences.getString(key, defaultValue)!!
    }

    private fun getEngelsystemShiftsUrl(): String {
        val key = context.getString(R.string.preference_key_engelsystem_json_export_url)
        val defaultValue = context.getString(R.string.preference_default_value_engelsystem_json_export_url)
        return preferences.getString(key, defaultValue)!!
    }
}
