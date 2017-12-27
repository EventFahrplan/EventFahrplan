package nerd.tuxmobil.fahrplan.congress.utils;

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
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.text.format.Time;
import android.widget.Toast;

import org.ligi.tracedroid.logging.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater;
import nerd.tuxmobil.fahrplan.congress.models.DateInfo;
import nerd.tuxmobil.fahrplan.congress.models.DateInfos;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.persistence.AlarmsDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.HighlightsTable;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.LecturesTable;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.MetasTable;
import nerd.tuxmobil.fahrplan.congress.persistence.HighlightDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.LecturesDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.MetaDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.wiki.WikiEventUtils;

public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";
    private static final int ALL_DAYS = -1;

    public static void loadDays(Context context) {
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

    public static void loadMeta(Context context) {
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

    public static String getEventUrl(final String eventId) {
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
        String links = lecture.getLinks();
        if (WikiEventUtils.linksContainWikiLink(links)) {
            links = links.replaceAll("\\),", ")<br>");
            links = StringUtils.getHtmlLinkFromMarkdown(links);
            sb.append(links);
        } else {
            String eventOnline = context.getString(R.string.event_online);
            sb.append(eventOnline);
            sb.append(": ");
            sb.append(getEventUrl(lecture.lecture_id));
        }
        return sb.toString();
    }

    private static long getLectureStartTime(@NonNull Lecture lecture) {
        long when;
        if (lecture.dateUTC > 0) {
            when = lecture.dateUTC;
        } else {
            Time time = lecture.getTime();
            when = time.normalize(true);
        }
        return when;
    }

    @SuppressLint("NewApi")
    public static void addToCalender(Context context, Lecture l) {
        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

        intent.putExtra(CalendarContract.Events.TITLE, l.title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, l.room);

        long startTime = getLectureStartTime(l);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + (l.duration * 60000));
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

    public static void deleteAlarm(@NonNull Context context, @NonNull Lecture lecture) {
        Log.d(FahrplanMisc.class.getName(), "Delete alarm for lecture: " + lecture);
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
        String lecture_id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
        int day = cursor.getInt(cursor.getColumnIndex(AlarmsTable.Columns.DAY));
        String title = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_TITLE));
        long startTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));

        Intent deleteAlarmIntent = new AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setLectureId(lecture_id)
                .setDay(day)
                .setTitle(title)
                .setStartTime(startTime)
                .setIsDeleteAlarm()
                .build();

        // delete any previous alarms of this lecture
        db.delete(AlarmsTable.NAME, AlarmsTable.Columns.EVENT_ID + "=?",
                new String[]{lecture.lecture_id});
        db.close();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent.getBroadcast(
                context, Integer.parseInt(lecture.lecture_id), deleteAlarmIntent, 0);
        // Cancel any existing alarms for this lecture
        alarmManager.cancel(pendingintent);

        lecture.has_alarm = false;
    }

    public static void addAlarm(@NonNull Context context,
                                @NonNull Lecture lecture,
                                int alarmTimesIndex) {
        Log.d(FahrplanMisc.class.getName(), "Add alarm for lecture: " + lecture +
                ", alarmTimesIndex: " + alarmTimesIndex);
        String[] alarm_times = context.getResources().getStringArray(R.array.alarm_time_values);
        List<String> alarmTimeStrings = new ArrayList<>(Arrays.asList(alarm_times));
        List<Integer> alarmTimes = new ArrayList<>(alarmTimeStrings.size());
        for (String alarmTimeString : alarmTimeStrings) {
            alarmTimes.add(Integer.parseInt(alarmTimeString));
        }
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
        long alarmTimeDiffInSeconds = alarmTimes.get(alarmTimesIndex) * 60 * 1000L;
        when -= alarmTimeDiffInSeconds;

        // DEBUG
        // when = System.currentTimeMillis() + (30 * 1000);

        time.set(when);
        MyApp.LogDebug("addAlarm",
                "Alarm time: " + time.format("%Y-%m-%dT%H:%M:%S%z") + ", in seconds: " + when);

        Intent addAlarmIntent = new AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setLectureId(lecture.lecture_id)
                .setDay(lecture.day)
                .setTitle(lecture.title)
                .setStartTime(startTime)
                .setIsAddAlarm()
                .build();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent.getBroadcast(
                context, Integer.parseInt(lecture.lecture_id), addAlarmIntent, 0);
        // Cancel any existing alarms for this lecture
        alarmManager.cancel(pendingintent);

        // Set new alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingintent);

        int alarmTimeInMin = alarmTimes.get(alarmTimesIndex);

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

    public static void writeHighlight(@NonNull Context context, @NonNull Lecture lecture) {
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

    public static void clearUpdateAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmintent = new Intent(context, AlarmReceiver.class);
        alarmintent.setAction(AlarmReceiver.ALARM_UPDATE);

        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmintent, 0);

        MyApp.LogDebug(LOG_TAG, "clear update alarm");

        alarmManager.cancel(pendingintent);
    }

    public static long setUpdateAlarm(Context context, boolean initial) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(AlarmReceiver.ALARM_UPDATE);
        final PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        MyApp.LogDebug(LOG_TAG, "set update alarm");
        Time t = new Time();
        t.setToNow();
        final long now = t.toMillis(true);

        AlarmUpdater alarmUpdater = new AlarmUpdater(MyApp.conferenceTimeFrame,
                new AlarmUpdater.OnAlarmUpdateListener() {

                    @Override
                    public void onCancelAlarm() {
                        MyApp.LogDebug(LOG_TAG, "cancel alarm post congress");
                        alarmManager.cancel(pendingintent);
                    }

                    @Override
                    public void onRescheduleAlarm(long interval, long nextFetch) {
                        MyApp.LogDebug(LOG_TAG, "update alarm to interval " + interval +
                                ", next in " + (nextFetch - now));
                        alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP, nextFetch, interval, pendingintent);
                    }

                    @Override
                    public void onRescheduleInitialAlarm(long interval, long nextFetch) {
                        MyApp.LogDebug(LOG_TAG, "set initial alarm to interval " + interval +
                                ", next in " + (nextFetch - now));
                        alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP, nextFetch, interval, pendingintent);
                    }
                });
        return alarmUpdater.calculateInterval(now, initial);
    }

    @NonNull
    public static List<Lecture> loadLecturesForAllDays(@NonNull Context context) {
        return loadLecturesForDayIndex(context, ALL_DAYS);
    }

    /**
     * Load all Lectures from the DB on the day specified
     *
     * @param context The Android Context
     * @param day     The day to load lectures for (0..), or -1 for all days
     * @return ArrayList of Lecture objects
     */
    @NonNull
    public static List<Lecture> loadLecturesForDayIndex(@NonNull Context context, int day) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        SQLiteDatabase lecturedb = null;
        LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);
        lecturedb = lecturesDB.getReadableDatabase();

        HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(context);
        SQLiteDatabase highlightdb = highlightDB.getReadableDatabase();

        List<Lecture> lectures = new ArrayList<>();
        Cursor cursor, hCursor;
        boolean allDays;

        allDays = day == ALL_DAYS;

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
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
        MyApp.LogDebug(LOG_TAG, "Got " + cursor.getCount() + " rows.");
        MyApp.LogDebug(LOG_TAG, "Got " + hCursor.getCount() + " highlight rows.");

        if (cursor.getCount() == 0) {
            cursor.close();
            lecturedb.close();
            highlightdb.close();
            lecturesDB.close();
            return Collections.emptyList();
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Lecture lecture = getLecture(cursor);
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
                    lecture.highlight = (highlightState == HighlightsTable.Values.HIGHLIGHT_STATE_ON);
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

    @NonNull
    private static Lecture getLecture(@NonNull Cursor cursor) {
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
        lecture.changedTitle = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TITLE)) != 0;
        lecture.changedSubtitle = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_SUBTITLE)) != 0;
        lecture.changedRoom = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_ROOM)) != 0;
        lecture.changedDay = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_DAY)) != 0;
        lecture.changedSpeakers = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_SPEAKERS)) != 0;
        lecture.changedRecordingOptOut = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_RECORDING_OPTOUT)) != 0;
        lecture.changedLanguage = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_LANGUAGE)) != 0;
        lecture.changedTrack = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TRACK)) != 0;
        lecture.changedIsNew = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_IS_NEW)) != 0;
        lecture.changedTime = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_TIME)) != 0;
        lecture.changedDuration = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_DURATION)) != 0;
        lecture.changedIsCanceled = cursor.getInt(
                cursor.getColumnIndex(LecturesTable.Columns.CHANGED_IS_CANCELED)) != 0;
        return lecture;
    }

    public static int getChangedLectureCount(@NonNull List<Lecture> list, boolean favsOnly) {
        int count = 0;
        if (list.isEmpty()) {
            return 0;
        }
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if (l.isChanged() && ((!favsOnly) || (l.highlight))) {
                count++;
            }
        }
        MyApp.LogDebug(LOG_TAG, "getChangedLectureCount " + favsOnly + ":" + count);
        return count;
    }

    public static int getNewLectureCount(@NonNull List<Lecture> list, boolean favsOnly) {
        int count = 0;
        if (list.isEmpty()) {
            return 0;
        }
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if ((l.changedIsNew) && ((!favsOnly) || (l.highlight))) count++;
        }
        MyApp.LogDebug(LOG_TAG, "getNewLectureCount " + favsOnly + ":" + count);
        return count;
    }

    public static int getCancelledLectureCount(@NonNull List<Lecture> list, boolean favsOnly) {
        int count = 0;
        if (list.isEmpty()) {
            return 0;
        }
        for (int lectureIndex = 0; lectureIndex < list.size(); lectureIndex++) {
            Lecture l = list.get(lectureIndex);
            if ((l.changedIsCanceled) && ((!favsOnly) || (l.highlight))) count++;
        }
        MyApp.LogDebug(LOG_TAG, "getCancelledLectureCount " + favsOnly + ":" + count);
        return count;
    }

    @NonNull
    public static List<Lecture> readChanges(Context context) {
        MyApp.LogDebug(LOG_TAG, "readChanges");
        List<Lecture> changesList = loadLecturesForAllDays(context);
        if (changesList.isEmpty()) {
            return changesList;
        }
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

    @NonNull
    public static List<Lecture> getStarredLectures(@NonNull Context context) {
        List<Lecture> starredList = loadLecturesForAllDays(context);
        if (starredList.isEmpty()) {
            return starredList;
        }
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
