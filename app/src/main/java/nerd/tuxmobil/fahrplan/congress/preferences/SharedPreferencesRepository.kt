package nerd.tuxmobil.fahrplan.congress.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys

class SharedPreferencesRepository(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setChangesSeen(changesSeen: Boolean) = with(preferences.edit()) {
        putBoolean(BundleKeys.PREFS_CHANGES_SEEN, changesSeen)
        apply()
    }

}
