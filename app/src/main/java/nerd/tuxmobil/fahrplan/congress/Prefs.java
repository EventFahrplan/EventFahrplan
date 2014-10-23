package nerd.tuxmobil.fahrplan.congress;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);
        findPreference("auto_update")
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(Prefs.this);

                        Editor edit = prefs.edit();
                        edit.putBoolean("auto_update", (Boolean) newValue);
                        edit.commit();

                        if ((Boolean) newValue) {
                            FahrplanMisc.setUpdateAlarm(Prefs.this, true);
                        } else {
                            FahrplanMisc.clearUpdateAlarm(Prefs.this);
                        }
                        return true;
                    }
                });
    }

}
