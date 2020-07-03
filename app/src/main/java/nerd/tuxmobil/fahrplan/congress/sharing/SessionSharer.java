package nerd.tuxmobil.fahrplan.congress.sharing;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import nerd.tuxmobil.fahrplan.congress.MyApp;

public class SessionSharer {

    // String formattedSessions can be one or multiple sessions
    public static boolean shareSimple(@NonNull Context context, @NonNull String formattedSessions) {
        return share(context, formattedSessions, "text/plain");
    }

    public static boolean shareJson(@NonNull Context context, @NonNull String formattedSessions) {
        return share(context, formattedSessions, "text/json");
    }

    private static boolean share(@NonNull Context context, @NonNull String formattedSessions, @NonNull String mimeType) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, formattedSessions);
        intent.setType(mimeType);
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) == null) {
            MyApp.LogDebug(SessionSharer.class.getSimpleName(), "No activity to handle share intent.");
            return false;
        } else {
            context.startActivity(intent);
            return true;
        }
    }

}
