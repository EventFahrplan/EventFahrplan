package nerd.tuxmobil.fahrplan.congress.system;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.text.format.Time;

import org.ligi.tracedroid.logging.Log;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityStateReceiver;
import nerd.tuxmobil.fahrplan.congress.persistence.AlarmsDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public final class OnBootReceiver extends BroadcastReceiver {

    final String LOG_TAG = "onBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        MyApp.LogDebug(LOG_TAG, "onReceive (reboot)");
        AlarmsDBOpenHelper lecturesDB = new AlarmsDBOpenHelper(context);

        SQLiteDatabase db = lecturesDB.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.query(AlarmsTable.NAME, AlarmsDBOpenHelper.allcolumns,
                    null, null, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            db.close();
            return;
        }

        Time now = new Time();
        Time storedAlarmTime = new Time();
        now.setToNow();
        now.second += 15;
        now.normalize(true);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long alarmTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));
            storedAlarmTime.set(alarmTime);

            if (now.before(storedAlarmTime)) {
                // set alarm

                String lecture_id = cursor.getString(
                        cursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
                int day = cursor.getInt(
                        cursor.getColumnIndex(AlarmsTable.Columns.DAY));
                String title = cursor.getString(
                        cursor.getColumnIndex(AlarmsTable.Columns.EVENT_TITLE));
                long startTime = cursor.getLong(
                        cursor.getColumnIndex(AlarmsTable.Columns.TIME));
                Log.d(getClass().getName(), "Set alarm for lecture: " + lecture_id);

                Intent addAlarmIntent = new AlarmReceiver.AlarmIntentBuilder()
                        .setContext(context)
                        .setLectureId(lecture_id)
                        .setDay(day)
                        .setTitle(title)
                        .setStartTime(startTime)
                        .setIsAddAlarm()
                        .build();

                PendingIntent pendingintent = PendingIntent.getBroadcast(
                        context, Integer.parseInt(lecture_id), addAlarmIntent, 0);
                MyApp.LogDebug(LOG_TAG, "add alarm for " + title);
                // Set new alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingintent);
            } else {
                // remove from DB

                MyApp.LogDebug(LOG_TAG, "remove alarm from DB ");

                String id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.ID));
                db.delete(AlarmsTable.NAME, AlarmsTable.Columns.ID + " = ?", new String[]{id});
            }

            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        if (ConnectivityStateReceiver.isEnabled(context)) {
            ConnectivityStateReceiver.disableReceiver(context);
        }

        // start auto updates
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean doAutoUpdates = prefs.getBoolean("auto_update", false);
        if (doAutoUpdates) {
            long lastFetch = prefs.getLong("last_fetch", 0);
            long nowMillis;
            now.setToNow();
            nowMillis = now.toMillis(true);

            long interval = FahrplanMisc.setUpdateAlarm(context, true);

            MyApp.LogDebug(LOG_TAG, "now: " + nowMillis + ", last_fetch: " + lastFetch);
            if ((interval > 0) && (nowMillis - lastFetch >= interval)) {
                UpdateService.start(context);
            }
        }
    }
}
