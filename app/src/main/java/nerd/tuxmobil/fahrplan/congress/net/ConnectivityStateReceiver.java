package nerd.tuxmobil.fahrplan.congress.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;

import static nerd.tuxmobil.fahrplan.congress.net.Connectivity.networkIsAvailable;

public class ConnectivityStateReceiver extends BroadcastReceiver {

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            return;
        }
        Log.d(getClass().getName(), "Received connection state event.");

        if (networkIsAvailable(context)) {
            Log.d(getClass().getName(), "Network is available.");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean defaultValue = context.getResources().getBoolean(R.bool.preferences_auto_update_enabled_default_value);
            boolean doAutoUpdates = prefs.getBoolean("auto_update", defaultValue);
            if (doAutoUpdates) {
                UpdateService.start(context);
            }
        }
    }

}
