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
    fun isAlternativeHighlightingEnabled(): Boolean
    fun setAlternativeHighlighting(enable: Boolean)
    fun isFastSwipingEnabled(): Boolean
    fun setFastSwiping(enable: Boolean)
    fun isShowScheduleUpdateDialogEnabled() : Boolean
    fun setShowScheduleUpdateDialog(enable: Boolean)
    fun isShowOnLockscreenEnabled(): Boolean
    fun setShowOnLockscreen(enable: Boolean)
    fun getAlarmTone(): Uri?
    fun setAlarmTone(alarmTone: Uri?)
    fun isInsistentAlarmsEnabled(): Boolean
    fun setInsistentAlarms(enable: Boolean)
    fun getAlarmTime(): Int
    fun setAlarmTime(alarmTime: Int)

    fun getScheduleRefreshInterval(): Int
    fun getScheduleRefreshIntervalDefaultValue(): Int
    fun setScheduleRefreshInterval(interval: Int)
    fun isAutoUpdateEnabled(): Boolean
    fun setAutoUpdateEnabled(enable: Boolean)
    fun getAlternativeScheduleUrl(): String
    fun setAlternativeScheduleUrl(url: String)
    fun getEngelsystemShiftsUrl(): String
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
