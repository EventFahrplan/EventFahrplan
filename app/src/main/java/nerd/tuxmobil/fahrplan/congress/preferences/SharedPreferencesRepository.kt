package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys

class SharedPreferencesRepository(val context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun getDisplayDayIndex() = preferences.getInt(BundleKeys.PREFS_DISPLAY_DAY_INDEX, 1)

    fun setDisplayDayIndex(displayDayIndex: Int) = preferences.edit {
        putInt(BundleKeys.PREFS_DISPLAY_DAY_INDEX, displayDayIndex)
    }

    fun getScheduleLastFetchedAt() =
            preferences.getLong(BundleKeys.PREFS_SCHEDULE_LAST_FETCHED_AT, 0)

    fun setScheduleLastFetchedAt(fetchedAt: Long) = preferences.edit {
        putLong(BundleKeys.PREFS_SCHEDULE_LAST_FETCHED_AT, fetchedAt)
    }

    fun getChangesSeen() =
            preferences.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true)

    fun setChangesSeen(changesSeen: Boolean) = preferences.edit {
        putBoolean(BundleKeys.PREFS_CHANGES_SEEN, changesSeen)
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
