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
        private val settingsDefaults = Settings()

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

    override fun getScheduleRefreshInterval(): Int {
        val key = context.getString(R.string.preference_key_schedule_refresh_interval_index)
        val defaultValue = settingsDefaults.scheduleRefreshInterval.toString()
        val value = preferences.getString(key, defaultValue)!!
        return value.toInt()
    }

    override fun getScheduleRefreshIntervalDefaultValue(): Int {
        return settingsDefaults.scheduleRefreshInterval
    }

    override fun getAlarmTime(): Int {
        val key = context.getString(R.string.preference_key_alarm_time_index)
        val defaultValue = settingsDefaults.alarmTime.toString()
        val value = preferences.getString(key, defaultValue)!!
        return value.toInt()
    }

    override fun getAlarmTone(): Uri? {
        val key = context.getString(R.string.preference_key_alarm_tone)
        val defaultValue = AlarmToneConversion.getPersistableString(settingsDefaults.alarmTone)
        val value = preferences.getString(key, defaultValue)!!
        return AlarmToneConversion.getPickerIntentUri(value)
    }

    override fun isUseDeviceTimeZoneEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_use_device_time_zone_enabled)
        val defaultValue = settingsDefaults.isUseDeviceTimeZoneEnabled
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isAlternativeHighlightingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_alternative_highlighting_enabled)
        val defaultValue = settingsDefaults.isAlternativeHighlightingEnabled
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isFastSwipingEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_fast_swiping_enabled)
        val defaultValue = settingsDefaults.isFastSwipingEnabled
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isAutoUpdateEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_auto_update_enabled)
        val defaultValue = settingsDefaults.isAutoUpdateEnabled
        return preferences.getBoolean(key, defaultValue)
    }

    override fun isInsistentAlarmsEnabled(): Boolean {
        val key = context.getString(R.string.preference_key_insistent_alarms_enabled)
        val defaultValue = settingsDefaults.isInsistentAlarmsEnabled
        return preferences.getBoolean(key, defaultValue)
    }

    override fun getAlternativeScheduleUrl(): String {
        val key = context.getString(R.string.preference_key_alternative_schedule_url)
        val defaultValue = settingsDefaults.alternativeScheduleUrl
        return preferences.getString(key, defaultValue)!!
    }

    override fun getEngelsystemShiftsUrl(): String {
        val key = context.getString(R.string.preference_key_engelsystem_json_export_url)
        val defaultValue = settingsDefaults.engelsystemShiftsUrl
        return preferences.getString(key, defaultValue)!!
    }
}
