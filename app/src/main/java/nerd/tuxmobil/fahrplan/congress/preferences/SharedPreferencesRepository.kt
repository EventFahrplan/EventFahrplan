package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys

class SharedPreferencesRepository(val context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setChangesSeen(changesSeen: Boolean) = with(preferences.edit()) {
        putBoolean(BundleKeys.PREFS_CHANGES_SEEN, changesSeen)
        apply()
    }

    fun getScheduleUrl(): String {
        val defaultScheduleUrl = context.getString(R.string.preferences_schedule_url_default_value)
        return preferences.getString(BundleKeys.PREFS_SCHEDULE_URL, defaultScheduleUrl)
    }

}
