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
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion

internal class DefaultSettingsRepository(
    context: Context,
) : SettingsRepository {

    companion object {
        private const val SCHEDULE_REFRESH_INTERVAL_KEY = "schedule_refresh_interval"
        private const val AUTO_UPDATE_ENABLED_KEY = "auto_update"
        private const val ALARM_TIME_KEY = "default_alarm_time"
        private const val ALARM_TONE_KEY = "reminder_tone"
        private const val USE_DEVICE_TIME_ZONE_KEY = "use_device_time_zone"
        private const val ALTERNATIVE_HIGHLIGHT_KEY = "alternative_highlight"
        private const val FAST_SWIPING_ENABLED_KEY = "fast_swiping"
        private const val ALTERNATIVE_SCHEDULE_URL_KEY = "schedule_url"
        private const val INSISTENT_ALARMS_ENABLED_KEY = "insistent"
        private const val SHOW_SCHEDULE_UPDATE_DIALOG_ENABLED_KEY = "show_schedule_update_dialog"
        private const val ENGELSYSTEM_URL_KEY = "preference_key_engelsystem_json_export_url"

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
            putBoolean(USE_DEVICE_TIME_ZONE_KEY, enable)
        }
    }

    override fun setAlternativeHighlighting(enable: Boolean) {
        preferences.edit {
            putBoolean(ALTERNATIVE_HIGHLIGHT_KEY, enable)
        }
    }

    override fun setFastSwiping(enable: Boolean) {
        preferences.edit {
            putBoolean(FAST_SWIPING_ENABLED_KEY, enable)
        }
    }

    override fun setShowScheduleUpdateDialog(enable: Boolean) {
        preferences.edit {
            putBoolean(SHOW_SCHEDULE_UPDATE_DIALOG_ENABLED_KEY, enable)
        }
    }

    override fun setAlarmTone(alarmTone: Uri?) {
        preferences.edit {
            val value = AlarmToneConversion.getPersistableString(alarmTone)
            putString(ALARM_TONE_KEY, value)
        }
    }

    override fun setInsistentAlarms(enable: Boolean) {
        preferences.edit {
            putBoolean(INSISTENT_ALARMS_ENABLED_KEY, enable)
        }
    }

    override fun setAlarmTime(alarmTime: Int) {
        preferences.edit {
            putString(ALARM_TIME_KEY, alarmTime.toString())
        }
    }

    override fun setScheduleRefreshInterval(interval: Int) {
        preferences.edit {
            putString(SCHEDULE_REFRESH_INTERVAL_KEY, interval.toString())
        }
    }

    override fun setAutoUpdateEnabled(enable: Boolean) {
        preferences.edit {
            putBoolean(AUTO_UPDATE_ENABLED_KEY, enable)
        }
    }

    override fun setAlternativeScheduleUrl(url: String) {
        preferences.edit {
            putString(ALTERNATIVE_SCHEDULE_URL_KEY, url)
        }
    }

    override fun setEngelsystemShiftsUrl(url: String) {
        preferences.edit {
            putString(ENGELSYSTEM_URL_KEY, url)
        }
    }

    private fun getSettings(): Settings {
        return Settings(
            isUseDeviceTimeZoneEnabled = isUseDeviceTimeZoneEnabled(),
            isAlternativeHighlightingEnabled = isAlternativeHighlightingEnabled(),
            isFastSwipingEnabled = isFastSwipingEnabled(),
            isShowScheduleUpdateDialogEnabled = isShowScheduleUpdateDialogEnabled(),
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
        val defaultValue = settingsDefaults.scheduleRefreshInterval.toString()
        val value = preferences.getString(SCHEDULE_REFRESH_INTERVAL_KEY, defaultValue)!!
        return value.toInt()
    }

    override fun getScheduleRefreshIntervalDefaultValue(): Int {
        return settingsDefaults.scheduleRefreshInterval
    }

    override fun getAlarmTime(): Int {
        val defaultValue = settingsDefaults.alarmTime.toString()
        val value = preferences.getString(ALARM_TIME_KEY, defaultValue)!!
        return value.toInt()
    }

    override fun getAlarmTone(): Uri? {
        val defaultValue = AlarmToneConversion.getPersistableString(settingsDefaults.alarmTone)
        val value = preferences.getString(ALARM_TONE_KEY, defaultValue)!!
        return AlarmToneConversion.getPickerIntentUri(value)
    }

    override fun isUseDeviceTimeZoneEnabled(): Boolean {
        val defaultValue = settingsDefaults.isUseDeviceTimeZoneEnabled
        return preferences.getBoolean(USE_DEVICE_TIME_ZONE_KEY, defaultValue)
    }

    override fun isAlternativeHighlightingEnabled(): Boolean {
        val defaultValue = settingsDefaults.isAlternativeHighlightingEnabled
        return preferences.getBoolean(ALTERNATIVE_HIGHLIGHT_KEY, defaultValue)
    }

    override fun isFastSwipingEnabled(): Boolean {
        val defaultValue = settingsDefaults.isFastSwipingEnabled
        return preferences.getBoolean(FAST_SWIPING_ENABLED_KEY, defaultValue)
    }

    override fun isShowScheduleUpdateDialogEnabled(): Boolean {
        val defaultValue = settingsDefaults.isShowScheduleUpdateDialogEnabled
        return preferences.getBoolean(SHOW_SCHEDULE_UPDATE_DIALOG_ENABLED_KEY, defaultValue)
    }

    override fun isAutoUpdateEnabled(): Boolean {
        val defaultValue = settingsDefaults.isAutoUpdateEnabled
        return preferences.getBoolean(AUTO_UPDATE_ENABLED_KEY, defaultValue)
    }

    override fun isInsistentAlarmsEnabled(): Boolean {
        val defaultValue = settingsDefaults.isInsistentAlarmsEnabled
        return preferences.getBoolean(INSISTENT_ALARMS_ENABLED_KEY, defaultValue)
    }

    override fun getAlternativeScheduleUrl(): String {
        val defaultValue = settingsDefaults.alternativeScheduleUrl
        return preferences.getString(ALTERNATIVE_SCHEDULE_URL_KEY, defaultValue)!!
    }

    override fun getEngelsystemShiftsUrl(): String {
        val defaultValue = settingsDefaults.engelsystemShiftsUrl
        return preferences.getString(ENGELSYSTEM_URL_KEY, defaultValue)!!
    }
}
