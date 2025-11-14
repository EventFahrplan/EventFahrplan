package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Store and retrieve user preferences (settings).
 */
interface SettingsRepository {
    val settingsStream: Flow<Settings>

    fun isUseDeviceTimeZoneEnabled(): Boolean
    fun setUseDeviceTimeZone(enable: Boolean)
    fun setAlternativeHighlighting(enable: Boolean)
    fun setFastSwiping(enable: Boolean)
    fun getAlarmTone(): Uri?
    fun setAlarmTone(alarmTone: Uri?)
    fun setInsistentAlarms(enable: Boolean)
    fun setAlarmTime(alarmTime: Int)

    fun setScheduleRefreshInterval(interval: Int)
    fun setAutoUpdateEnabled(enable: Boolean)
    fun setAlternativeScheduleUrl(url: String)
    fun setEngelsystemShiftsUrl(url: String)

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultSettingsRepository(context).also { instance = it }
            }
        }
    }
}
