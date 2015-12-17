package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));

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
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {

                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());

                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putBoolean("auto_update", (Boolean) newValue);
                            edit.commit();

                            if ((Boolean) newValue) {
                                FahrplanMisc.setUpdateAlarm(getActivity(), true);
                            } else {
                                FahrplanMisc.clearUpdateAlarm(getActivity());
                            }
                            return true;
                        }
                    });


            findPreference("schedule_url").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {


                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(BundleKeys.PREFS_SCHEDULE_URL, (String)newValue);
                    edit.commit();
                    return true;
                }
            });

            findPreference("alternative_highlight")
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {

                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());

                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putBoolean(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, (Boolean) newValue);
                            edit.apply();

                            Intent redrawIntent = new Intent();
                            redrawIntent.putExtra(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, true);
                            getActivity().setResult(Activity.RESULT_OK, redrawIntent);

                            return true;
                        }
                    });
        }
    }
}
