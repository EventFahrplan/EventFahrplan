package nerd.tuxmobil.fahrplan.congress.net;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;

import static nerd.tuxmobil.fahrplan.congress.net.Connectivity.networkIsAvailable;

public class ConnectivityStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            return;
        }
        Log.d(getClass().getName(), "Received connection state event.");

        if (networkIsAvailable(context)) {
            Log.d(getClass().getName(), "Network is available.");

            disableReceiver(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean defaultValue = context.getResources().getBoolean(R.bool.preferences_auto_update_enabled_default_value);
            boolean doAutoUpdates = prefs.getBoolean("auto_update", defaultValue);
            if (doAutoUpdates) {
                UpdateService.start(context);
            }
        }
    }

    public static void disableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, ConnectivityStateReceiver.class);
        PackageManager manager = context.getPackageManager();
        manager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, ConnectivityStateReceiver.class);
        PackageManager manager = context.getPackageManager();
        manager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isEnabled(Context context) {
        ComponentName receiver = new ComponentName(context, ConnectivityStateReceiver.class);
        PackageManager manager = context.getPackageManager();
        int enabled = manager.getComponentEnabledSetting(receiver);
        switch (enabled) {
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
            default:
                return false;
        }
    }
}
