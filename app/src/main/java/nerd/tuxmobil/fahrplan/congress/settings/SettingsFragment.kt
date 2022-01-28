package nerd.tuxmobil.fahrplan.congress.settings

import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.preferences.AlarmTonePreference
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc

class SettingsFragment : PreferenceFragmentCompat() {

    private companion object {

        const val REQUEST_CODE_ALARM_TONE = 3439

    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screen = requirePreference<PreferenceScreen>(getString(R.string.preference_key_screen))
        val developmentCategory = requirePreference<PreferenceCategory>(getString(R.string.preference_key_category_development))
        requirePreference<ListPreference>(resources.getString(R.string.preference_key_schedule_refresh_interval_index)).onPreferenceChangeListener = OnPreferenceChangeListener { _, _ ->
            coroutineScope.launch {
                delay(100) // Workaround because preference is written asynchronous.
                FahrplanMisc.setUpdateAlarm(requireContext(), true)
            }
            true
        }
        if (!BuildConfig.DEBUG) {
            screen.removePreference(developmentCategory)
        }

        val categoryGeneral = requirePreference<PreferenceCategory>(getString(R.string.preference_key_category_general))

        requirePreference<SwitchPreferenceCompat>(resources.getString(R.string.preference_key_auto_update_enabled)).onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            val isAutoUpdateEnabled = newValue as Boolean
            if (isAutoUpdateEnabled) {
                FahrplanMisc.setUpdateAlarm(requireContext(), true)
            } else {
                with(requireActivity()) {
                    AlarmServices.newInstance(requireContext(), AppRepository).discardAutoUpdateAlarm()
                }
            }
            true
        }

        requirePreference<SwitchPreferenceCompat>(resources.getString(R.string.preference_key_use_device_time_zone_enabled)).onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any ->
            requestRedraw(BundleKeys.USE_DEVICE_TIME_ZONE_UPDATED)
            true
        }

        val appNotificationSettingsPreference = requirePreference<Preference>(getString(R.string.preference_key_app_notification_settings))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            categoryGeneral.removePreference(appNotificationSettingsPreference)
        } else {
            appNotificationSettingsPreference.onPreferenceClickListener = OnPreferenceClickListener { preference: Preference ->
                launchAppNotificationSettings(preference.context)
                true
            }
        }

        val alternativeScheduleUrlPreference = requirePreference<EditTextPreference>(getString(R.string.preference_key_alternative_schedule_url))
        if (BuildConfig.ENABLE_ALTERNATIVE_SCHEDULE_URL) {
            alternativeScheduleUrlPreference.onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requestRedraw(BundleKeys.SCHEDULE_URL_UPDATED)
                true
            }
            alternativeScheduleUrlPreference.summaryProvider = SummaryProvider<EditTextPreference> {
                when (it.text.isEmpty()) {
                    true -> getString(R.string.preference_summary_alternative_schedule_url)
                    false -> it.text
                }
            }
        } else {
            categoryGeneral.removePreference(alternativeScheduleUrlPreference)
        }

        requirePreference<SwitchPreferenceCompat>(resources.getString(R.string.preference_key_alternative_highlighting_enabled)).onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
            requestRedraw(BundleKeys.ALTERNATIVE_HIGHLIGHTING_UPDATED)
            true
        }

        val engelsystemCategory = requirePreference<PreferenceCategory>(getString(R.string.preference_engelsystem_category_key))
        if (BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS) {
            val urlPreference = requirePreference<EditTextPreference>(getString(R.string.preference_key_engelsystem_json_export_url))
            urlPreference.summaryProvider = SummaryProvider<EditTextPreference> {
                when (it.text.isEmpty()) {
                    true -> getString(R.string.preference_summary_engelsystem_json_export_url).toSpanned()
                    false -> "${it.text.dropLast(23)}..." // Truncate to keep the key private.
                }
            }
            urlPreference.onPreferenceChangeListener = OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requestRedraw(BundleKeys.ENGELSYSTEM_SHIFTS_URL_UPDATED)
                true
            }
        } else {
            screen.removePreference(engelsystemCategory)
        }
    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is AlarmTonePreference) {
            preference.showAlarmTonePicker(this, REQUEST_CODE_ALARM_TONE)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (REQUEST_CODE_ALARM_TONE == requestCode && RESULT_OK == resultCode && intent != null) {
            val preference = requirePreference<AlarmTonePreference>(getString(R.string.preference_key_alarm_tone))
            preference.onAlarmTonePicked(intent)
        } else {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }

    private fun requestRedraw(bundleKey: String) {
        val redrawIntent = Intent().withExtras(bundleKey to true)
        requireNotNull(activity).setResult(RESULT_OK, redrawIntent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun launchAppNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).withExtras(
                Settings.EXTRA_APP_PACKAGE to context.packageName
        )
        startActivity(intent)
    }

    /**
     * Returns a [Preference] for the given key or throws a [NullPointerException]
     * if none can be found. Uses [findPreference].
     */
    private fun <T : Preference> requirePreference(key: CharSequence): T = findPreference(key)
            ?: throw NullPointerException("Cannot find preference for '$key' key.")

}
