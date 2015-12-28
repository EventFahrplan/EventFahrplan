package nerd.tuxmobil.fahrplan.congress;

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
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.HighlightsTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.MetasTable;

public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";
    private static final int ALL_DAYS = -1;

    static void loadDays(Context context) {
        MyApp.dateInfos = new DateInfos();
        LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

        SQLiteDatabase lecturedb = lecturesDB.getReadableDatabase();
        Cursor cursor;

        try {
            cursor = lecturedb.query(LecturesTable.NAME, LecturesDBOpenHelper.allcolumns,
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
            int day = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.DAY));
            String date = cursor.getString(cursor.getColumnIndex(LecturesTable.Columns.DATE));

            DateInfo dateItem = new DateInfo(day, date);
            if (!MyApp.dateInfos.contains(dateItem)) {
                MyApp.dateInfos.add(dateItem);
            }
            cursor.moveToNext();
        }
        cursor.close();

        for (DateInfo dateInfo : MyApp.dateInfos) {
            MyApp.LogDebug(LOG_TAG, "DateInfo: " + dateInfo);
        }
        lecturesDB.close();
        lecturedb.close();
    }

    static void loadMeta(Context context) {
        MetaDBOpenHelper metaDB = new MetaDBOpenHelper(context);
        SQLiteDatabase metadb = metaDB.getReadableDatabase();

        Cursor cursor;
        try {
            cursor = metadb.query(MetasTable.NAME, MetaDBOpenHelper.allcolumns, null, null,
                    null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            metaDB.close();
            metadb.close();
            metadb = null;
            return;
        }

        MyApp.numdays = MetasTable.Defaults.NUM_DAYS_DEFAULT;
        MyApp.version = "";
        MyApp.title = "";
        MyApp.subtitle = "";
        MyApp.dayChangeHour = MetasTable.Defaults.DAY_CHANGE_HOUR_DEFAULT;
        MyApp.dayChangeMinute = MetasTable.Defaults.DAY_CHANGE_MINUTE_DEFAULT;
        MyApp.eTag = null;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnIndexNumDays = cursor.getColumnIndex(MetasTable.Columns.NUM_DAYS);
            if (cursor.getColumnCount() > columnIndexNumDays) {
                MyApp.numdays = cursor.getInt(columnIndexNumDays);
            }
            int columIndexVersion = cursor.getColumnIndex(MetasTable.Columns.VERSION);
            if (cursor.getColumnCount() > columIndexVersion) {
                MyApp.version = cursor.getString(columIndexVersion);
            }
            int columnIndexTitle = cursor.getColumnIndex(MetasTable.Columns.TITLE);
            if (cursor.getColumnCount() > columnIndexTitle) {
                MyApp.title = cursor.getString(columnIndexTitle);
            }
            int columnIndexSubTitle = cursor.getColumnIndex(MetasTable.Columns.SUBTITLE);
            if (cursor.getColumnCount() > columnIndexSubTitle) {
                MyApp.subtitle = cursor.getString(columnIndexSubTitle);
            }
            int columnIndexDayChangeHour = cursor
                    .getColumnIndex(MetasTable.Columns.DAY_CHANGE_HOUR);
            if (cursor.getColumnCount() > columnIndexDayChangeHour) {
                MyApp.dayChangeHour = cursor.getInt(columnIndexDayChangeHour);
            }
            int columnIndexDayChangeMinute = cursor
                    .getColumnIndex(MetasTable.Columns.DAY_CHANGE_MINUTE);
            if (cursor.getColumnCount() > columnIndexDayChangeMinute) {
                MyApp.dayChangeMinute = cursor.getInt(columnIndexDayChangeMinute);
            }
            int columnIndexEtag = cursor.getColumnIndex(MetasTable.Columns.ETAG);
            if (cursor.getColumnCount() > columnIndexEtag) {
                MyApp.eTag = cursor.getString(columnIndexEtag);
            }
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
        sb.append(l.title).append("\n").append(SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT)
                .format(new Date(time.toMillis(true))));
        sb.append(", ").append(l.room).append("\n\n");
        final String eventUrl = getEventUrl(context, l.lecture_id);
        sb.append(eventUrl);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static String getEventUrl(final Context context, final String eventId) {
        StringBuilder sb = new StringBuilder();
        sb.append(BuildConfig.SCHEDULE_DOMAIN_PART);
        sb.append(BuildConfig.SCHEDULE_PART);
        // TODO The event url can be localized by providing individual values
        // for `schedule_event_part` in `values` and `values-de`.
        String eventPart = String.format(BuildConfig.SCHEDULE_EVENT_PART, eventId);
        sb.append(eventPart);
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
                    new String[]{lecture.lecture_id},
                    null,
                    null,
                    null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            MyApp.LogDebug("delete alarm", "failure on alarm query");
            db.close();
            return;
        }

        if (cursor.getCount() == 0) {
            db.close();
            cursor.close();
            MyApp.LogDebug("delete_alarm", "alarm for " + lecture.lecture_id + " not found");
            lecture.has_alarm = false;
            return;
        }

        cursor.moveToFirst();

        Intent intent = new Intent(context, AlarmReceiver.class);

        String lecture_id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
        intent.putExtra(BundleKeys.ALARM_LECTURE_ID, lecture_id);
        int day = cursor.getInt(cursor.getColumnIndex(AlarmsTable.Columns.DAY));
        intent.putExtra(BundleKeys.ALARM_DAY, day);
        String title = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_TITLE));
        intent.putExtra(BundleKeys.ALARM_TITLE, title);
        long startTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));
        intent.putExtra(BundleKeys.ALARM_START_TIME, startTime);

        // delete any previous alarms of this lecture
        db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?",
                new String[]{lecture.lecture_id});
        db.close();

        intent.setAction("de.machtnix.fahrplan.ALARM");
        intent.setData(Uri.parse("alarm://" + lecture.lecture_id));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent
                .getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

        // Cancel any existing alarms for this lecture
        alarmManager.cancel(pendingintent);

        lecture.has_alarm = false;
    }

    public static void addAlarm(Context context, Lecture lecture, int alarmTimesIndex) {
        int[] alarm_times = context.getResources().getIntArray(R.array.alarm_time_values);
        long when;
        Time time;
        long startTime;
        long startTimeInSeconds = lecture.dateUTC;

        if (startTimeInSeconds > 0) {
            when = startTimeInSeconds;
            startTime = startTimeInSeconds;
            time = new Time();
        } else {
            time = lecture.getTime();
            startTime = time.normalize(true);
            when = time.normalize(true);
        }
        long alarmTimeDiffInSeconds = alarm_times[alarmTimesIndex] * 60 * 1000;
        when -= alarmTimeDiffInSeconds;

        // DEBUG
        // when = System.currentTimeMillis() + (30 * 1000);

        time.set(when);
        MyApp.LogDebug("addAlarm",
                "Alarm time: " + time.format("%Y-%m-%dT%H:%M:%S%z") + ", in seconds: " + when);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(BundleKeys.ALARM_LECTURE_ID, lecture.lecture_id);
        intent.putExtra(BundleKeys.ALARM_DAY, lecture.day);
        intent.putExtra(BundleKeys.ALARM_TITLE, lecture.title);
        intent.putExtra(BundleKeys.ALARM_START_TIME, startTime);
        intent.setAction(AlarmReceiver.ALARM_LECTURE);

        intent.setData(Uri.parse("alarm://" + lecture.lecture_id));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent
                .getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

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
            db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?",
                    new String[]{lecture.lecture_id});

            ContentValues values = new ContentValues();

            values.put(AlarmsTable.Columns.EVENT_ID, Integer.parseInt(lecture.lecture_id));
            values.put(AlarmsTable.Columns.EVENT_TITLE, lecture.title);
            values.put(AlarmsTable.Columns.ALARM_TIME_IN_MIN, alarmTimeInMin);
            values.put(AlarmsTable.Columns.TIME, when);
            DateFormat df = SimpleDateFormat
                    .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
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
            db.delete(HighlightsTable.NAME, HighlightsTable.Columns.EVENT_ID + "=?",
                    new String[]{lecture.lecture_id});

            ContentValues values = new ContentValues();

            values.put(HighlightsTable.Columns.EVENT_ID, Integer.parseInt(lecture.lecture_id));
            int highlightState = lecture.highlight ? HighlightsTable.Values.HIGHLIGHT_STATE_ON
                    : HighlightsTable.Values.HIGHLIGHT_STATE_OFF;
            values.put(HighlightsTable.Columns.HIGHLIGHT, highlightState);

            db.insert(HighlightsTable.NAME, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public static long setUpdateAlarm(Context context, boolean initial) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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

        if ((now < MyApp.first_day_start) && ((now + AlarmManager.INTERVAL_DAY)
                >= MyApp.first_day_start)) {
            next_fetch = MyApp.first_day_start;
            interval = 2 * AlarmManager.INTERVAL_HOUR;
            if (!initial) {
                MyApp.LogDebug(LOG_TAG,
                        "update alarm to interval " + interval + ", next in " + (next_fetch - now));
                alarmManager.cancel(pendingintent);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next_fetch, interval,
                        pendingintent);
            }
        }

        if (initial) {
            MyApp.LogDebug(LOG_TAG,
                    "set initial alarm to interval " + interval + ", next in " + (next_fetch
                            - now));
            alarmManager.cancel(pendingintent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next_fetch, interval,
                    pendingintent);
        }

        return interval;
    }

    public static void clearUpdateAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmintent = new Intent(context, AlarmReceiver.class);
        alarmintent.setAction(AlarmReceiver.ALARM_UPDATE);

        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmintent, 0);

        MyApp.LogDebug(LOG_TAG, "clear update alarm");

        alarmManager.cancel(pendingintent);
    }

    public static LectureList loadLecturesForAllDays(Context context) {
        return loadLecturesForDayIndex(context, ALL_DAYS);
    }

    /**
     * Load all Lectures from the DB on the day specified
     *
     * @param context The Android Context
     * @param day The day to load lectures for (0..), or -1 for all days
     * @return ArrayList of Lecture objects
     */
    public static LectureList loadLecturesForDayIndex(Context context, int day) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        SQLiteDatabase lecturedb = null;
        LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);
        lecturedb = lecturesDB.getReadableDatabase();

        HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(context);
        SQLiteDatabase highlightdb = highlightDB.getReadableDatabase();

        LectureList lectures = new LectureList();
        Cursor cursor, hCursor;
        boolean allDays;

        if (day == ALL_DAYS) {
            allDays = true;
        } else {
            allDays = false;
        }

        try {
            cursor = lecturedb.query(
                    LecturesTable.NAME,
                    LecturesDBOpenHelper.allcolumns,
                    allDays ? null : (LecturesTable.Columns.DAY + "=?"),
                    allDays ? null : (new String[]{String.format("%d", day)}),
                    null, null, LecturesTable.Columns.DATE_UTC);
        } catch (SQLiteException e) {
            e.printStackTrace();
            lecturedb.close();
            highlightdb.close();
            lecturesDB.close();
            return null;
        }
        try {
            hCursor = highlightdb.query(
                    HighlightsTable.NAME,
                    HighlightDBOpenHelper.allcolumns,
                    null, null, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            lecturedb.close();
            highlightdb.close();
            lecturesDB.close();
            return null;
        }
        MyApp.LogDebug(LOG_TAG, "Got " + cursor.getCount() + " rows.");
        MyApp.LogDebug(LOG_TAG, "Got " + hCursor.getCount() + " highlight rows.");

        if (cursor.getCount() == 0) {
            cursor.close();
            lecturedb.close();
            highlightdb.close();
            lecturesDB.close();
            return null;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Lecture lecture = new Lecture(cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.EVENT_ID)));
            lecture.title = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.TITLE));
            lecture.subtitle = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.SUBTITLE));
            lecture.day = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.DAY));
            lecture.room = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.ROOM));
            lecture.startTime = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.START));
            lecture.duration = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.DURATION));
            lecture.speakers = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.SPEAKERS));
            lecture.track = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.TRACK));
            lecture.type = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.TYPE));
            lecture.lang = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.LANG));
            lecture.abstractt = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.ABSTRACT));
            lecture.description = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.DESCR));
            lecture.relStartTime = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.REL_START));
            lecture.date = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.DATE));
            lecture.links = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.LINKS));
            lecture.dateUTC = cursor.getLong(
                    cursor.getColumnIndex(LecturesTable.Columns.DATE_UTC));
            lecture.room_index = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.ROOM_IDX));
            lecture.recordingLicense = cursor.getString(
                    cursor.getColumnIndex(LecturesTable.Columns.REC_LICENSE));
            lecture.recordingOptOut = cursor.getInt(
                    cursor.getColumnIndex(LecturesTable.Columns.REC_OPTOUT))
                    == LecturesTable.Values.REC_OPTOUT_OFF
                    ? Lecture.RECORDING_OPTOUT_OFF
                    : Lecture.RECORDING_OPTOUT_ON;
            lecture.changedTitle = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TITLE)) != 0;
            lecture.changedSubtitle = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_SUBTITLE)) != 0;
            lecture.changedRoom = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_ROOM)) != 0;
            lecture.changedDay = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_DAY)) != 0;
            lecture.changedSpeakers = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_SPEAKERS)) != 0;
            lecture.changedRecordingOptOut = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_RECORDING_OPTOUT)) != 0;
            lecture.changedLanguage = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_LANGUAGE)) != 0;
            lecture.changedTrack = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TRACK)) != 0;
            lecture.changedIsNew = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_IS_NEW)) != 0;
            lecture.changedTime = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TIME)) != 0;
            lecture.changedDuration = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_DURATION)) != 0;
            lecture.changedIsCanceled = cursor.getInt(cursor.getColumnIndex(LecturesTable.Columns.CHANGED_IS_CANCELED)) != 0;

            lectures.add(lecture);
            cursor.moveToNext();
        }
        cursor.close();

        hCursor.moveToFirst();
        while (!hCursor.isAfterLast()) {
            String lecture_id = hCursor.getString(
                    hCursor.getColumnIndex(HighlightsTable.Columns.EVENT_ID));
            int highlightState = hCursor.getInt(
                    hCursor.getColumnIndex(HighlightsTable.Columns.HIGHLIGHT));
            MyApp.LogDebug(LOG_TAG, "lecture " + lecture_id + " is hightlighted:" + highlightState);

            for (Lecture lecture : lectures) {
                if (lecture.lecture_id.equals(lecture_id)) {
                    lecture.highlight = (highlightState
                            == HighlightsTable.Values.HIGHLIGHT_STATE_ON ? true : false);
                }
            }
            hCursor.moveToNext();
        }
        hCursor.close();

        highlightdb.close();
        lecturedb.close();
        lecturesDB.close();
        return lectures;
    }

    public static int getChangedLectureCount(LectureList list, boolean favsOnly) {
        int count = 0;
        if (list == null) return 0;
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if (l.isChanged() && ((!favsOnly) || (l.highlight))) {
                count++;
            }
        }
        MyApp.LogDebug(LOG_TAG, "getChangedLectureCount " + favsOnly + ":" + count);
        return count;
    }

    public static int getNewLectureCount(LectureList list, boolean favsOnly) {
        int count = 0;
        if (list == null) return 0;
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if ((l.changedIsNew) && ((!favsOnly) || (l.highlight))) count++;
        }
        MyApp.LogDebug(LOG_TAG, "getNewLectureCount " + favsOnly + ":" + count);
        return count;
    }

    public static int getCancelledLectureCount(LectureList list, boolean favsOnly) {
        int count = 0;
        if (list == null) return 0;
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if ((l.changedIsCanceled) && ((!favsOnly) || (l.highlight))) count++;
        }
        MyApp.LogDebug(LOG_TAG, "getCancelledLectureCount " + favsOnly + ":" + count);
        return count;
    }

    public static LectureList readChanges(Context context) {
        MyApp.LogDebug(LOG_TAG, "readChanges");
        LectureList changesList = FahrplanMisc.loadLecturesForAllDays(context);
        if (changesList == null) return null;
        int lectureIndex = changesList.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = changesList.get(lectureIndex);
            if (!l.isChanged() && !l.changedIsCanceled && !l.changedIsNew) {
                changesList.remove(l);
            }
            lectureIndex--;
        }
        MyApp.LogDebug(LOG_TAG, changesList.size() + " lectures changed.");
        return changesList;
    }

    public static LectureList getStarredLectures(Context context) {
        LectureList starredList = FahrplanMisc.loadLecturesForAllDays(context);
        if (starredList == null) return null;
        int lectureIndex = starredList.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = starredList.get(lectureIndex);
            if (!l.highlight) {
                starredList.remove(l);
            }
            lectureIndex--;
        }
        MyApp.LogDebug(LOG_TAG, starredList.size() + " lectures starred.");
        return starredList;
    }
}
