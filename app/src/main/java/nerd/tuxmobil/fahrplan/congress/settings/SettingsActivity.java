package nerd.tuxmobil.fahrplan.congress.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.extensions.Strings;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public class SettingsActivity extends BaseActivity {

    private static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
            MyApp.LogDebug(LOG_TAG, "onCreate fragment created");
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            PreferenceCategory categoryGeneral = (PreferenceCategory) findPreference(getString(R.string.preference_key_category_general));

            findPreference(getResources().getString(R.string.preference_key_auto_update_enabled))
                    .setOnPreferenceChangeListener((preference, newValue) -> {
                        Boolean isAutoUpdateEnabled = (Boolean) newValue;
                        if (isAutoUpdateEnabled) {
                            FahrplanMisc.setUpdateAlarm(getActivity(), true);
                        } else {
                            AlarmManager alarmManager = Contexts.getAlarmManager(getActivity());
                            new AlarmServices(alarmManager).discardAutoUpdateAlarm(getActivity());
                        }
                        return true;
                    });

            Preference appNotificationSettingsPreference = findPreference(getString(R.string.preference_key_app_notification_settings));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                categoryGeneral.removePreference(appNotificationSettingsPreference);
            } else {
                appNotificationSettingsPreference.setOnPreferenceClickListener(preference -> {
                    launchAppNotificationSettings(preference.getContext());
                    return true;
                });
            }

            Preference alternativeScheduleUrlPreference = findPreference(getString(R.string.preference_key_alternative_schedule_url));
            if (BuildConfig.ENABLE_ALTERNATIVE_SCHEDULE_URL) {
                alternativeScheduleUrlPreference
                        .setOnPreferenceChangeListener((preference, newValue) -> {
                            Intent redrawIntent = new Intent();
                            redrawIntent.putExtra(BundleKeys.BUNDLE_KEY_SCHEDULE_URL_UPDATED, true);
                            getActivity().setResult(Activity.RESULT_OK, redrawIntent);
                            return true;
                        });
            } else {
                categoryGeneral.removePreference(alternativeScheduleUrlPreference);
            }

            findPreference(getResources().getString(R.string.preference_key_alternative_highlighting_enabled))
                    .setOnPreferenceChangeListener((preference, newValue) -> {
                        Intent redrawIntent = new Intent();
                        redrawIntent.putExtra(BundleKeys.BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED, true);
                        getActivity().setResult(Activity.RESULT_OK, redrawIntent);
                        return true;
                    });

            PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.preference_key_screen));
            PreferenceCategory engelsystemCategory = (PreferenceCategory) findPreference(getString(R.string.preference_engelsystem_category_key));
            if (BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS) {
                Preference urlPreference = findPreference(getString(R.string.preference_key_engelsystem_json_export_url));
                urlPreference.setSummary(Strings.toSpanned(getString(R.string.preference_summary_engelsystem_json_export_url)));
                urlPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    Intent redrawIntent = new Intent();
                    redrawIntent.putExtra(BundleKeys.BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED, true);
                    getActivity().setResult(Activity.RESULT_OK, redrawIntent);
                    return true;
                });
            } else {
                screen.removePreference(engelsystemCategory);
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void launchAppNotificationSettings(Context context) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            startActivity(intent);
        }

    }
}
