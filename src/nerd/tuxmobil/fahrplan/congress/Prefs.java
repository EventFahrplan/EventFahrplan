package nerd.tuxmobil.fahrplan.congress;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Prefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);
        //getListView().setBackgroundResource(R.drawable.back);
        //getListView().setCacheColorHint(Color.TRANSPARENT);
    }

}
