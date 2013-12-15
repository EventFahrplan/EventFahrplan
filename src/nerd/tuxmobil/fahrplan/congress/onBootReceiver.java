package nerd.tuxmobil.fahrplan.congress;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.format.Time;

public final class onBootReceiver extends BroadcastReceiver {

	final String LOG_TAG = "onBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
    	MyApp.LogDebug(LOG_TAG, "onReceive (reboot)");
		AlarmsDBOpenHelper lecturesDB = new AlarmsDBOpenHelper(context);

		SQLiteDatabase db = lecturesDB.getWritableDatabase();
		Cursor cursor;

		try {
			cursor = db.query("alarms", AlarmsDBOpenHelper.allcolumns,
					null, null, null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			db.close();
			return;
		}

		Time now = new Time();
		Time alarm = new Time();
		now.setToNow();
		now.second += 15;
		now.normalize(true);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long alarmTime = cursor.getLong(2);
			alarm.set(alarmTime);

			if (now.before(alarm)) {
				// set alarm
				String lecture_id = cursor.getString(4);
				int day = cursor.getInt(6);
				String title = cursor.getString(1);
				long startTime = cursor.getLong(5);

				Intent alarmintent = new Intent(context, AlarmReceiver.class);
				alarmintent.putExtra("lecture_id", lecture_id);
				alarmintent.putExtra("day", day);
				alarmintent.putExtra("title", title);
				alarmintent.putExtra("startTime", startTime);

				alarmintent.setAction("de.machtnix.fahrplan.ALARM");
				alarmintent.setData(Uri.parse("alarm://"+lecture_id));

				PendingIntent pendingintent = PendingIntent.getBroadcast(context, Integer.parseInt(lecture_id), alarmintent, 0);

				MyApp.LogDebug(LOG_TAG, "add alarm for "+title);

				// Set new alarm
				alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingintent);
			} else {
				// remove from DB

				MyApp.LogDebug(LOG_TAG, "remove alarm from DB ");

				String id = cursor.getString(0);
				db.delete("alarms","_id = ?", new String[]{id});
			}

			cursor.moveToNext();
		}
		cursor.close();
		db.close();
    }
}
