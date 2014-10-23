package nerd.tuxmobil.fahrplan.congress;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class ConnectivityStateReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ConnectivityStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        MyApp.LogDebug(LOG_TAG, "got Conn State event");

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if ((networkInfo != null) && (networkInfo.isConnected())) {
            MyApp.LogDebug(LOG_TAG, "is connected");

            disableReceiver(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean do_auto_updates = prefs.getBoolean("auto_update", false);
            if (do_auto_updates) {
                Intent updateIntent = new Intent(context, UpdateService.class);
                context.startService(updateIntent);
            }
        }
    }

    public static void disableReceiver(Context ctx) {
        final PackageManager pm;
        pm = ctx.getPackageManager();
        ComponentName receiver = new ComponentName(ctx, ConnectivityStateReceiver.class);
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableReceiver(Context ctx) {
        final PackageManager pm;
        pm = ctx.getPackageManager();
        ComponentName receiver = new ComponentName(ctx, ConnectivityStateReceiver.class);
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isEnabled(Context ctx) {
        final PackageManager pm;
        pm = ctx.getPackageManager();
        ComponentName connReceiver = new ComponentName(ctx, ConnectivityStateReceiver.class);
        int enabled = pm.getComponentEnabledSetting(connReceiver);
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
