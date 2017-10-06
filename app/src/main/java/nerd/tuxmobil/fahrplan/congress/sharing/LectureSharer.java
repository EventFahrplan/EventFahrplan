package nerd.tuxmobil.fahrplan.congress.sharing;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import nerd.tuxmobil.fahrplan.congress.MyApp;

public class LectureSharer {

    // String formattedLectures can be one or multiple lectures
    public static boolean shareSimple(@NonNull Context context, @NonNull String formattedLectures) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, formattedLectures);
        intent.setType("text/plain");
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) == null) {
            MyApp.LogDebug(LectureSharer.class.getName(), "No activity to handle share intent.");
            return false;
        } else {
            context.startActivity(intent);
            return true;
        }
    }

}
