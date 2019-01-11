package nerd.tuxmobil.fahrplan.congress.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public class SettingsActivity extends BaseActivity {

    private static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            findPreference("auto_update")
                    .setOnPreferenceChangeListener((preference, newValue) -> {
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(getActivity());

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putBoolean("auto_update", (Boolean) newValue);
                        edit.commit();

                        if ((Boolean) newValue) {
                            FahrplanMisc.setUpdateAlarm(getActivity(), true);
                        } else {
                            AlarmServices.discardAutoUpdateAlarm(getActivity());
                        }
                        return true;
                    });

            Preference appNotificationSettingsPreference = findPreference(getString(R.string.preference_key_app_notification_settings));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                PreferenceCategory categoryGeneral = (PreferenceCategory) findPreference(getString(R.string.preference_key_category_general));
                categoryGeneral.removePreference(appNotificationSettingsPreference);
            } else {
                appNotificationSettingsPreference.setOnPreferenceClickListener(preference -> {
                    launchAppNotificationSettings(preference.getContext());
                    return true;
                });
            }

            findPreference("schedule_url")
                    .setOnPreferenceChangeListener((preference, newValue) -> {
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(getActivity());

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(BundleKeys.PREFS_SCHEDULE_URL, (String) newValue);
                        edit.commit();
                        return true;
                    });

            findPreference("alternative_highlight")
                    .setOnPreferenceChangeListener((preference, newValue) -> {

                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(getActivity());

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putBoolean(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, (Boolean) newValue);
                        edit.apply();

                        Intent redrawIntent = new Intent();
                        redrawIntent.putExtra(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, true);
                        getActivity().setResult(Activity.RESULT_OK, redrawIntent);

                        return true;
                    });

            findPreference("default_alarm_time")
                    .setOnPreferenceChangeListener((preference, newValue) -> {
                        Activity activity = getActivity();
                        Resources resources = activity.getResources();
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(activity);
                        SharedPreferences.Editor edit = prefs.edit();
                        int alarmTimeValue = Integer.parseInt((String) newValue);
                        String[] alarmTimeValues = resources.getStringArray(R.array.alarm_time_values);
                        int defaultAlarmTimeValue = resources.getInteger(R.integer.default_alarm_time_index);
                        int alarmTimeIndex = getAlarmTimeIndex(alarmTimeValues, alarmTimeValue, defaultAlarmTimeValue);
                        edit.putInt(BundleKeys.PREFS_ALARM_TIME_INDEX, alarmTimeIndex);
                        edit.commit();
                        return true;
                    });
        }

        private int getAlarmTimeIndex(String[] alarmTimeValues, int alarmTimeValue, int defaultAlarmTimeValue) {
            for (int index = 0, alarmTimeValuesLength = alarmTimeValues.length; index < alarmTimeValuesLength; index++) {
                String alarmTimeStringValue = alarmTimeValues[index];
                int alarmTimeIntegerValue = Integer.parseInt(alarmTimeStringValue);
                if (alarmTimeIntegerValue == alarmTimeValue) {
                    return index;
                }
            }
            return defaultAlarmTimeValue;
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void launchAppNotificationSettings(Context context) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            startActivity(intent);
        }

    }
}
