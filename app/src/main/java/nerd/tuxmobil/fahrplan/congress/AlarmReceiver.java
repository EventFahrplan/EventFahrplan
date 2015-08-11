package nerd.tuxmobil.fahrplan.congress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;

public final class AlarmReceiver extends BroadcastReceiver {

    public static final String ALARM_LECTURE = "nerd.tuxmobil.fahrplan.congress.ALARM_LECTURE";

    public static final String ALARM_UPDATE = "nerd.tuxmobil.fahrplan.congress.ALARM_UPDATE";

    private static final String LOG_TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        MyApp.LogDebug(LOG_TAG, "got alarm");
        MyApp.LogDebug(LOG_TAG, "action " + intent.getAction());

        if (intent.getAction().equals(ALARM_LECTURE)) {
            String lecture_id = intent.getStringExtra(BundleKeys.ALARM_LECTURE_ID);
            int lid = Integer.parseInt(lecture_id);
            int day = intent.getIntExtra(BundleKeys.ALARM_DAY, 1);
            long when = intent
                    .getLongExtra(BundleKeys.ALARM_START_TIME, System.currentTimeMillis());
            String title = intent.getStringExtra(BundleKeys.ALARM_TITLE);
            //Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notify = new Notification();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean insistent = prefs.getBoolean("insistent", false);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra("lecture_id", lecture_id);
            notificationIntent.putExtra("day", day);
            notificationIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, lid, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            notify = builder.setSound(Uri.parse(prefs.getString("reminder_tone", "")))
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getResources().getColor(R.color.colorAccent))
                    .setContentIntent(contentIntent)
                    .setContentText(context.getString(R.string.reminder))
                    .setContentTitle(title)
                    .setWhen(when).build();

            MyApp.LogDebug("alarm", "insistent is " + insistent);
            if (insistent) {
                notify.flags |= Notification.FLAG_INSISTENT;
            }

            nm.notify(1, notify);

            // Clear from alarmDB

            AlarmsDBOpenHelper lecturesDB = new AlarmsDBOpenHelper(context);

            SQLiteDatabase db = lecturesDB.getReadableDatabase();
            db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?",
                    new String[]{lecture_id});

            db.close();

            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().reloadAlarms();
                MainActivity.getInstance().refreshEventMarkers();
            }
        } else if (intent.getAction().equals(ALARM_UPDATE)) {
            Intent updateIntent = new Intent(context, UpdateService.class);
            context.startService(updateIntent);
        }
    }
}
