package nerd.tuxmobil.fahrplan.congress.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.format.Time;

import org.ligi.tracedroid.logging.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater;
import nerd.tuxmobil.fahrplan.congress.dataconverters.AlarmExtensions;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.DateInfo;
import nerd.tuxmobil.fahrplan.congress.models.DateInfos;
import nerd.tuxmobil.fahrplan.congress.models.Highlight;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;

import static kotlin.collections.CollectionsKt.count;
import static kotlin.collections.CollectionsKt.filterNot;


public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";
    @VisibleForTesting
    public static final int ALL_DAYS = -1;
    private static final DateFormat TIME_TEXT_DATE_FORMAT =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    public static void loadDays(Context context) {
        MyApp.dateInfos = new DateInfos();
        List<DateInfo> dateInfos = AppRepository.Companion.getInstance(context).readDateInfos();
        for (DateInfo dateInfo : dateInfos) {
            if (!MyApp.dateInfos.contains(dateInfo)) {
                MyApp.dateInfos.add(dateInfo);
            }
        }
        for (DateInfo dateInfo : MyApp.dateInfos) {
            MyApp.LogDebug(LOG_TAG, "DateInfo: " + dateInfo);
        }
    }

    public static long getLectureStartTime(@NonNull Lecture lecture) {
        long when;
        if (lecture.dateUTC > 0) {
            when = lecture.dateUTC;
        } else {
            Time time = lecture.getTime();
            when = time.normalize(true);
        }
        return when;
    }

    public static void deleteAlarm(@NonNull Context context, @NonNull Lecture lecture) {
        AppRepository appRepository = AppRepository.Companion.getInstance(context);
        String eventId = lecture.lectureId;
        List<Alarm> alarms = appRepository.readAlarms(eventId);
        if (!alarms.isEmpty()) {
            // Delete any previous alarms of this event.
            Alarm alarm = alarms.get(0);
            SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
            AlarmServices.discardEventAlarm(context, schedulableAlarm);
            appRepository.deleteAlarmForEventId(eventId);
        }
        lecture.hasAlarm = false;
    }

    public static void addAlarm(@NonNull Context context,
                                @NonNull Lecture lecture,
                                int alarmTimesIndex) {
        Log.d(FahrplanMisc.class.getName(), "Add alarm for lecture: " + lecture.lectureId +
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

        String eventId = lecture.lectureId;
        String eventTitle = lecture.title;
        int alarmTimeInMin = alarmTimes.get(alarmTimesIndex);
        String timeText = TIME_TEXT_DATE_FORMAT.format(new Date(when));
        int day = lecture.day;

        Alarm alarm = new Alarm(alarmTimeInMin, day, startTime, eventId, eventTitle, when, timeText);
        SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
        AlarmServices.scheduleEventAlarm(context, schedulableAlarm, true);
        AppRepository.Companion.getInstance(context).updateAlarm(alarm);
        lecture.hasAlarm = true;
    }

    public static long setUpdateAlarm(Context context, boolean initial) {
        final AlarmManager alarmManager = Contexts.getAlarmManager(context);
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
    public static List<Lecture> loadLecturesForAllDays(@NonNull AppRepository appRepository) {
        return loadLecturesForDayIndex(appRepository, ALL_DAYS);
    }

    /**
     * Load all Lectures from the DB on the day specified
     *
     * @param appRepository The application repository to retrieve lectures from.
     * @param day           The day to load lectures for (0..), or -1 for all days
     * @return ArrayList of Lecture objects
     */
    @NonNull
    public static List<Lecture> loadLecturesForDayIndex(@NonNull AppRepository appRepository, int day) {
        List<Lecture> lectures;
        if (day == ALL_DAYS) {
            MyApp.LogDebug(LOG_TAG, "Loading lectures for all days.");
            lectures = appRepository.readLecturesOrderedByDateUtc();
        } else {
            MyApp.LogDebug(LOG_TAG, "Loading lectures for day " + day + ".");
            lectures = appRepository.readLecturesForDayIndexOrderedByDateUtc(day);
        }
        MyApp.LogDebug(LOG_TAG, "Got " + lectures.size() + " rows.");

        List<Highlight> highlights = appRepository.readHighlights();
        for (Highlight highlight : highlights) {
            MyApp.LogDebug(LOG_TAG, highlight.toString());
            for (Lecture lecture : lectures) {
                if (lecture.lectureId.equals("" + highlight.getEventId())) {
                    lecture.highlight = highlight.isHighlight();
                }
            }
        }
        return lectures;
    }

    public static int getChangedLectureCount(@NonNull final List<Lecture> list, boolean favsOnly) {
        int count = count(list, event -> event.isChanged() && (!favsOnly || event.highlight));
        MyApp.LogDebug(LOG_TAG, count + " changed lectures, favsOnly = " + favsOnly);
        return count;
    }

    public static int getNewLectureCount(@NonNull final List<Lecture> list, boolean favsOnly) {
        int count = count(list, event -> event.changedIsNew && (!favsOnly || event.highlight));
        MyApp.LogDebug(LOG_TAG, count + " new lectures, favsOnly = " + favsOnly);
        return count;
    }

    public static int getCancelledLectureCount(@NonNull final List<Lecture> list, boolean favsOnly) {
        int count = count(list, event -> event.changedIsCanceled && (!favsOnly || event.highlight));
        MyApp.LogDebug(LOG_TAG, count + " canceled lectures, favsOnly = " + favsOnly);
        return count;
    }

    @NonNull
    public static List<Lecture> readChanges(@NonNull AppRepository appRepository) {
        MyApp.LogDebug(LOG_TAG, "readChanges");
        List<Lecture> changesList = loadLecturesForAllDays(appRepository);
        if (changesList.isEmpty()) {
            return changesList;
        }
        changesList = filterNot(changesList, event -> !event.isChanged() && !event.changedIsCanceled && !event.changedIsNew);
        MyApp.LogDebug(LOG_TAG, changesList.size() + " lectures changed.");
        return changesList;
    }

    @NonNull
    public static List<Lecture> getStarredLectures(@NonNull AppRepository appRepository) {
        List<Lecture> starredList = loadLecturesForAllDays(appRepository);
        if (starredList.isEmpty()) {
            return starredList;
        }
        starredList = filterNot(starredList, event -> !event.highlight || event.changedIsCanceled);
        MyApp.LogDebug(LOG_TAG, starredList.size() + " lectures starred.");
        return starredList;
    }

    @NonNull
    public static List<Lecture> getUncanceledLectures(@NonNull AppRepository appRepository, int dayIndex) {
        List<Lecture> lectures = FahrplanMisc.loadLecturesForDayIndex(appRepository, dayIndex);
        return filterNot(lectures, event -> event.changedIsCanceled);
    }

}
