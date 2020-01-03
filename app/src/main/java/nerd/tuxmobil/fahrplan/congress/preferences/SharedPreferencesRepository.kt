package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys

class SharedPreferencesRepository(val context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun getScheduleLastFetchedAt() =
            preferences.getLong(BundleKeys.PREFS_SCHEDULE_LAST_FETCHED_AT, 0)

    fun setScheduleLastFetchedAt(fetchedAt: Long) = with(preferences.edit()) {
        putLong(BundleKeys.PREFS_SCHEDULE_LAST_FETCHED_AT, fetchedAt)
        apply()
    }

    fun getChangesSeen() =
            preferences.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true)

    fun setChangesSeen(changesSeen: Boolean) = with(preferences.edit()) {
        putBoolean(BundleKeys.PREFS_CHANGES_SEEN, changesSeen)
        apply()
    }

    fun getScheduleUrl(): String {
        val defaultScheduleUrl = context.getString(R.string.preference_schedule_url_default_value)
        return preferences.getString(BundleKeys.PREFS_SCHEDULE_URL, defaultScheduleUrl)!!
    }

    fun getEngelsystemShiftsUrl(): String {
        val defaultShiftsUrl = context.getString(R.string.preference_engelsystem_json_export_url_default_value)
        return preferences.getString(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_URL, defaultShiftsUrl)!!
    }

    fun setEngelsystemShiftsUrl(url: String) = with(preferences.edit()) {
        putString(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_URL, url)
        apply()
    }

    fun getLastEngelsystemShiftsHash() =
            preferences.getInt(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_HASH, 0)

    fun setLastEngelsystemShiftsHash(hash: Int) = with(preferences.edit()) {
        putInt(BundleKeys.PREFS_ENGELSYSTEM_SHIFTS_HASH, hash)
        apply()
    }

}
