package nerd.tuxmobil.fahrplan.congress.settings

import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.provider.Settings
import androidx.core.os.bundleOf
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.prefs)

        val categoryGeneral = findPreference(getString(R.string.preference_key_category_general)) as PreferenceCategory

        findPreference(resources.getString(R.string.preference_key_auto_update_enabled)).onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            val isAutoUpdateEnabled = newValue as Boolean
            if (isAutoUpdateEnabled) {
                FahrplanMisc.setUpdateAlarm(activity, true)
            } else {
                val alarmManager = activity.getAlarmManager()
                AlarmServices(alarmManager).discardAutoUpdateAlarm(activity)
            }
            true
        }

        val appNotificationSettingsPreference = findPreference(getString(R.string.preference_key_app_notification_settings))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            categoryGeneral.removePreference(appNotificationSettingsPreference)
        } else {
            appNotificationSettingsPreference.onPreferenceClickListener = OnPreferenceClickListener { preference: Preference ->
                launchAppNotificationSettings(preference.context)
                true
            }
        }

        val alternativeScheduleUrlPreference = findPreference(getString(R.string.preference_key_alternative_schedule_url))
        if (BuildConfig.ENABLE_ALTERNATIVE_SCHEDULE_URL) {
            alternativeScheduleUrlPreference.onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requestRedraw(BundleKeys.BUNDLE_KEY_SCHEDULE_URL_UPDATED)
                true
            }
        } else {
            categoryGeneral.removePreference(alternativeScheduleUrlPreference)
        }

        findPreference(resources.getString(R.string.preference_key_alternative_highlighting_enabled)).onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
            requestRedraw(BundleKeys.BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED)
            true
        }

        val screen = findPreference(getString(R.string.preference_key_screen)) as PreferenceScreen
        val engelsystemCategory = findPreference(getString(R.string.preference_engelsystem_category_key)) as PreferenceCategory
        if (BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS) {
            val urlPreference = findPreference(getString(R.string.preference_key_engelsystem_json_export_url))
            urlPreference.summary = getString(R.string.preference_summary_engelsystem_json_export_url).toSpanned()
            urlPreference.onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requestRedraw(BundleKeys.BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED)
                true
            }
        } else {
            screen.removePreference(engelsystemCategory)
        }
    }

    private fun requestRedraw(bundleKey: String) {
        val extras = bundleOf(bundleKey to true)
        val redrawIntent = Intent().apply { putExtras(extras) }
        requireNotNull(activity).setResult(RESULT_OK, redrawIntent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun launchAppNotificationSettings(context: Context) {
        val extras = bundleOf(Settings.EXTRA_APP_PACKAGE to context.packageName)
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtras(extras) }
        startActivity(intent)
    }
}
