package nerd.tuxmobil.fahrplan.congress;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.widget.Toast;

public class FahrplanMisc {

	private static final String LOG_TAG = "FahrplanMisc";

	static void loadDays(Context context) {
		MyApp.dateList = new ArrayList<DateList>();
		LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

		SQLiteDatabase lecturedb = lecturesDB.getReadableDatabase();
		Cursor cursor;

		try {
			cursor = lecturedb.query("lectures", LecturesDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			lecturesDB.close();
			return;
		}

		if (cursor.getCount() == 0) {
			// evtl. Datenbankreset wg. DB Formatänderung -> neu laden
			cursor.close();
			lecturesDB.close();
			lecturedb.close();
			return;
		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int day = cursor.getInt(3);
			String date = cursor.getString(14);

			if (DateList.dateInList(MyApp.dateList, day) == false) {
				MyApp.dateList.add(new DateList(day, date));
			}
			cursor.moveToNext();
		}
		cursor.close();

		for (DateList dayL : MyApp.dateList) {
			MyApp.LogDebug(LOG_TAG, "date day " + dayL.dayIdx + " = " + dayL.date);
		}
		lecturesDB.close();
		lecturedb.close();
	}

	static void loadMeta(Context context) {
		MetaDBOpenHelper metaDB = new MetaDBOpenHelper(context);
		SQLiteDatabase metadb = metaDB.getReadableDatabase();

		Cursor cursor;
		try {
			cursor = metadb.query("meta", MetaDBOpenHelper.allcolumns, null, null,
					null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			metaDB.close();
			metadb.close();
			metadb = null;
			return;
		}

		MyApp.numdays = 0;
		MyApp.version = "";
		MyApp.title = "";
		MyApp.subtitle = "";
		MyApp.dayChangeHour = 4;
		MyApp.dayChangeMinute = 0;
		MyApp.eTag = null;

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			if (cursor.getColumnCount() > 0)
				MyApp.numdays = cursor.getInt(0);
			if (cursor.getColumnCount() > 1)
				MyApp.version = cursor.getString(1);
			if (cursor.getColumnCount() > 2)
				MyApp.title = cursor.getString(2);
			if (cursor.getColumnCount() > 3)
				MyApp.subtitle = cursor.getString(3);
			if (cursor.getColumnCount() > 4)
				MyApp.dayChangeHour = cursor.getInt(4);
			if (cursor.getColumnCount() > 5)
				MyApp.dayChangeMinute = cursor.getInt(5);
			if (cursor.getColumnCount() > 6)
				MyApp.eTag = cursor.getString(6);
		}

		MyApp.LogDebug(LOG_TAG, "loadMeta: numdays=" + MyApp.numdays + " version:"
				+ MyApp.version + " " + MyApp.title + " " + MyApp.eTag);
		cursor.close();

		metadb.close();
		metaDB.close();
	}

	public static void share(Context context, Lecture l) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		StringBuilder sb = new StringBuilder();
		Time time = l.getTime();
		sb.append(l.title).append("\n").append(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT).format(new Date(time.toMillis(true))));
		sb.append(", ").append(l.room).append("\n\n");
		final String eventUrl = getEventUrl(context, l.lecture_id);
		sb.append(eventUrl);
		sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
		sendIntent.setType("text/plain");
		context.startActivity(sendIntent);
	}

	public static String getEventUrl(final Context context, final String eventId) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.schedule_domain_part));
		sb.append(context.getString(R.string.schedule_part));
		// TODO The event url can be localized by providing individual values
		// for `schedule_event_part` in `values` and `values-de`.
		sb.append(context.getString(R.string.schedule_event_part, eventId));
		return sb.toString();
	}

	public static String getCalendarDescription(final Context context, final Lecture lecture) {
		StringBuilder sb = new StringBuilder();
		sb.append(lecture.description);
		sb.append("\n\n");
		final String eventOnline = context.getString(R.string.event_online);
		sb.append(eventOnline + ": ");
		sb.append(getEventUrl(context, lecture.lecture_id));
		return sb.toString();
	}

	@SuppressLint("NewApi")
	public static void addToCalender(Context context, Lecture l) {
		Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

		intent.putExtra(CalendarContract.Events.TITLE, l.title);
		intent.putExtra(CalendarContract.Events.EVENT_LOCATION, l.room);

		long when;
		if (l.dateUTC > 0) {
			when = l.dateUTC;
		} else {
			Time time = l.getTime();
			when = time.normalize(true);
		}
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, when);
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, when + (l.duration * 60000));
		final String description = getCalendarDescription(context, l);
		intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
		try {
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
		}
		intent.setAction(Intent.ACTION_EDIT);
		try {
			context.startActivity(intent);
			return;
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.add_to_calendar_failed, Toast.LENGTH_LONG).show();
		}
	}

	public static void deleteAlarm(Context context, Lecture lecture) {
		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);
		SQLiteDatabase db = alarmDB.getWritableDatabase();
		Cursor cursor;

		try {
		cursor = db.query(
				AlarmsTable.NAME,
				AlarmsDBOpenHelper.allcolumns,
				AlarmsTable.Columns.EVENT_ID + "=?",
				new String[] { lecture.lecture_id },
				null,
				null,
				null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			MyApp.LogDebug("delete alarm","failure on alarm query");
			db.close();
			return;
		}

		if (cursor.getCount() == 0) {
			db.close();
			cursor.close();
			MyApp.LogDebug("delete_alarm", "alarm for " + lecture.lecture_id + " not found");
			return;
		}

		cursor.moveToFirst();

		Intent intent = new Intent(context, AlarmReceiver.class);
		String lecture_id = cursor.getString(4);
		intent.putExtra("lecture_id", lecture_id);
		int day = cursor.getInt(6);
		intent.putExtra("day", day);
		String title = cursor.getString(1);
		intent.putExtra("title", title);
		long startTime = cursor.getLong(5);
		intent.putExtra("startTime", startTime);
		// delete any previous alarms of this lecture
		db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?", new String[] { lecture.lecture_id });
		db.close();

		intent.setAction("de.machtnix.fahrplan.ALARM");
		intent.setData(Uri.parse("alarm://"+lecture.lecture_id));

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);

		lecture.has_alarm = false;
	}

	public static void addAlarm(Context context, Lecture lecture, int alarmTimesIndex) {
		int[] alarm_times = { 0, 5, 10, 15, 30, 45, 60 };
		long when;
		Time time;
		long startTime;
		if (lecture.dateUTC > 0) {
			when = lecture.dateUTC;
			startTime = lecture.dateUTC;
			time = new Time();
		} else {
			time = lecture.getTime();
			startTime = time.normalize(true);
			when = time.normalize(true);
		}
		when -= (alarm_times[alarmTimesIndex] * 60 * 1000);

		// DEBUG
		// when = System.currentTimeMillis() + (30 * 1000);

		time.set(when);
		MyApp.LogDebug("addAlarm", "Alarm time: "+when);


		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("lecture_id", lecture.lecture_id);
		intent.putExtra("day", lecture.day);
		intent.putExtra("title", lecture.title);
		intent.putExtra("startTime", startTime);
		intent.setAction(AlarmReceiver.ALARM_LECTURE);

		intent.setData(Uri.parse("alarm://"+lecture.lecture_id));

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);

		// Set new alarm
		alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingintent);

		int alarmTimeInMin = alarm_times[alarmTimesIndex];

		// write to DB

		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);

		SQLiteDatabase db = alarmDB.getWritableDatabase();

		// delete any previous alarms of this lecture
		try {
			db.beginTransaction();
			db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?", new String[] { lecture.lecture_id });

			ContentValues values = new ContentValues();

			values.put(AlarmsTable.Columns.EVENT_ID, Integer.parseInt(lecture.lecture_id));
			values.put(AlarmsTable.Columns.EVENT_TITLE, lecture.title);
			values.put(AlarmsTable.Columns.ALARM_TIME_IN_MIN, alarmTimeInMin);
			values.put(AlarmsTable.Columns.TIME, when);
			DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
			values.put(AlarmsTable.Columns.TIME_TEXT, df.format(new Date(when)));
			values.put(AlarmsTable.Columns.DISPLAY_TIME, startTime);
			values.put(AlarmsTable.Columns.DAY, lecture.day);

			db.insert(AlarmsTable.NAME, null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}

		lecture.has_alarm = true;
	}

	public static void writeHighlight(Context context, Lecture lecture) {
		HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(context);

		SQLiteDatabase db = highlightDB.getWritableDatabase();

		try {
			db.beginTransaction();
			db.delete("highlight", "eventid=?", new String[] { lecture.lecture_id });

			ContentValues values = new ContentValues();

			values.put("eventid", Integer.parseInt(lecture.lecture_id));
			values.put("highlight", lecture.highlight ? 1 : 0);

			db.insert("highlight", null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public static long setUpdateAlarm(Context context, boolean initial) {
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent alarmintent = new Intent(context, AlarmReceiver.class);
		alarmintent.setAction(AlarmReceiver.ALARM_UPDATE);

		PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmintent, 0);

		MyApp.LogDebug(LOG_TAG, "set update alarm");
		long next_fetch;
		long interval;
		Time t = new Time();
		t.setToNow();
		long now = t.toMillis(true);

		if ((now >= MyApp.first_day_start) && (now < MyApp.last_day_end)) {
			interval = 2 * AlarmManager.INTERVAL_HOUR;
			next_fetch = now + interval;
		} else if (now >= MyApp.last_day_end) {
			MyApp.LogDebug(LOG_TAG, "cancel alarm post congress");
			alarmManager.cancel(pendingintent);
			return 0;
		} else {
			interval = AlarmManager.INTERVAL_DAY;
			next_fetch = now + interval;
		}

		if ((now < MyApp.first_day_start) && ((now + AlarmManager.INTERVAL_DAY) >= MyApp.first_day_start)) {
			next_fetch = MyApp.first_day_start;
			interval = 2 * AlarmManager.INTERVAL_HOUR;
			if (!initial) {
				MyApp.LogDebug(LOG_TAG, "update alarm to interval " + interval + ", next in " + (next_fetch-now));
				alarmManager.cancel(pendingintent);
				alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next_fetch, interval, pendingintent);
			}
		}

		if (initial) {
			MyApp.LogDebug(LOG_TAG, "set initial alarm to interval " + interval + ", next in " + (next_fetch-now));
			alarmManager.cancel(pendingintent);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next_fetch, interval, pendingintent);
		}

		return interval;
	}

	public static void clearUpdateAlarm(Context context) {
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent alarmintent = new Intent(context, AlarmReceiver.class);
		alarmintent.setAction(AlarmReceiver.ALARM_UPDATE);

		PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmintent, 0);

		MyApp.LogDebug(LOG_TAG, "clear update alarm");

		alarmManager.cancel(pendingintent);
	}

}
