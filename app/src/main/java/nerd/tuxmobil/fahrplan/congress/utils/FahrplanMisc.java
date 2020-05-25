package nerd.tuxmobil.fahrplan.congress.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.ligi.tracedroid.logging.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
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
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;


public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";
    private static final DateFormat TIME_TEXT_DATE_FORMAT =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    public static void loadDays(@NonNull AppRepository appRepository) {
        MyApp.dateInfos = new DateInfos();
        List<DateInfo> dateInfos = appRepository.readDateInfos();
        for (DateInfo dateInfo : dateInfos) {
            if (!MyApp.dateInfos.contains(dateInfo)) {
                MyApp.dateInfos.add(dateInfo);
            }
        }
        for (DateInfo dateInfo : MyApp.dateInfos) {
            MyApp.LogDebug(LOG_TAG, "DateInfo: " + dateInfo);
        }
    }

    public static void deleteAlarm(@NonNull Context context, @NonNull AppRepository appRepository, @NonNull Lecture lecture) {
        String eventId = lecture.lectureId;
        List<Alarm> alarms = appRepository.readAlarms(eventId);
        if (!alarms.isEmpty()) {
            // Delete any previous alarms of this event.
            Alarm alarm = alarms.get(0);
            SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
            AlarmManager alarmManager = Contexts.getAlarmManager(context);
            new AlarmServices(alarmManager).discardEventAlarm(context, schedulableAlarm);
            appRepository.deleteAlarmForEventId(eventId);
        }
        lecture.hasAlarm = false;
        appRepository.notifyAlarmsChanged();
    }

    public static void addAlarm(@NonNull Context context,
                                @NonNull AppRepository appRepository,
                                @NonNull Lecture lecture,
                                int alarmTimesIndex) {
        Log.d(LOG_TAG, "Add alarm for lecture = " + lecture.lectureId +
                ", alarmTimesIndex = " + alarmTimesIndex + ".");
        String[] alarm_times = context.getResources().getStringArray(R.array.alarm_time_values);
        List<String> alarmTimeStrings = new ArrayList<>(Arrays.asList(alarm_times));
        List<Integer> alarmTimes = new ArrayList<>(alarmTimeStrings.size());
        for (String alarmTimeString : alarmTimeStrings) {
            alarmTimes.add(Integer.parseInt(alarmTimeString));
        }
        long when;
        Moment moment;
        long startTime;
        long startTimeInMilliSec = lecture.dateUTC;

        if (startTimeInMilliSec > 0) {
            when = startTimeInMilliSec;
            startTime = startTimeInMilliSec;
            moment = new Moment();
        } else {
            moment = lecture.getStartTimeMoment();
            startTime = moment.toMilliseconds();
            when = moment.toMilliseconds();
        }
        long alarmTimeDiffInSeconds = alarmTimes.get(alarmTimesIndex) * 60 * 1000L;
        when -= alarmTimeDiffInSeconds;

        moment.setToMilliseconds(when);
        MyApp.LogDebug(LOG_TAG, "Add alarm: Time = " + moment.toUTCDateTime() + ", in seconds = " + when + ".");

        String eventId = lecture.lectureId;
        String eventTitle = lecture.title;
        int alarmTimeInMin = alarmTimes.get(alarmTimesIndex);
        String timeText = TIME_TEXT_DATE_FORMAT.format(new Date(when));
        int day = lecture.day;

        Alarm alarm = new Alarm(alarmTimeInMin, day, startTime, eventId, eventTitle, when, timeText);
        SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
        AlarmManager alarmManager = Contexts.getAlarmManager(context);
        new AlarmServices(alarmManager).scheduleEventAlarm(context, schedulableAlarm, true);
        appRepository.updateAlarm(alarm);
        lecture.hasAlarm = true;
        appRepository.notifyAlarmsChanged();
    }

    public static long setUpdateAlarm(Context context, boolean initial) {
        final AlarmManager alarmManager = Contexts.getAlarmManager(context);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(AlarmReceiver.ALARM_UPDATE);
        final PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        MyApp.LogDebug(LOG_TAG, "set update alarm");
        final long now = new Moment().toMilliseconds();

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

}
