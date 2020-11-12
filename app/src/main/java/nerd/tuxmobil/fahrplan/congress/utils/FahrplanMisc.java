package nerd.tuxmobil.fahrplan.congress.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import org.ligi.tracedroid.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
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
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;


public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";

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

    public static void deleteAlarm(@NonNull Context context, @NonNull AppRepository appRepository, @NonNull Session session) {
        String sessionId = session.sessionId;
        List<Alarm> alarms = appRepository.readAlarms(sessionId);
        if (!alarms.isEmpty()) {
            // Delete any previous alarms of this session.
            Alarm alarm = alarms.get(0);
            SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
            AlarmManager alarmManager = Contexts.getAlarmManager(context);
            new AlarmServices(alarmManager).discardSessionAlarm(context, schedulableAlarm);
            appRepository.deleteAlarmForSessionId(sessionId);
        }
        session.hasAlarm = false;
        appRepository.notifyAlarmsChanged();
    }

    public static void addAlarm(@NonNull Context context,
                                @NonNull AppRepository appRepository,
                                @NonNull Session session,
                                int alarmTimesIndex) {
        Log.d(LOG_TAG, "Add alarm for session = " + session.sessionId +
                ", alarmTimesIndex = " + alarmTimesIndex + ".");
        String[] alarm_times = context.getResources().getStringArray(R.array.preference_entry_values_alarm_time);
        List<String> alarmTimeStrings = new ArrayList<>(Arrays.asList(alarm_times));
        List<Integer> alarmTimes = new ArrayList<>(alarmTimeStrings.size());
        for (String alarmTimeString : alarmTimeStrings) {
            alarmTimes.add(Integer.parseInt(alarmTimeString));
        }

        long sessionStartTime = session.getStartTimeMilliseconds();

        long alarmTimeOffset = alarmTimes.get(alarmTimesIndex) * 60 * 1000L;
        long alarmTime = sessionStartTime - alarmTimeOffset;

        Moment moment = Moment.ofEpochMilli(alarmTime);
        MyApp.LogDebug(LOG_TAG, "Add alarm: Time = " + moment.toUtcDateTime() + ", in seconds = " + alarmTime + ".");

        String sessionId = session.sessionId;
        String sessionTitle = session.title;
        int alarmTimeInMin = alarmTimes.get(alarmTimesIndex);
        String timeText = DateFormatter.newInstance().getFormattedDateTimeShort(alarmTime);
        int day = session.day;

        Alarm alarm = new Alarm(alarmTimeInMin, day, sessionStartTime, sessionId, sessionTitle, alarmTime, timeText);
        SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
        AlarmManager alarmManager = Contexts.getAlarmManager(context);
        new AlarmServices(alarmManager).scheduleSessionAlarm(context, schedulableAlarm, true);
        appRepository.updateAlarm(alarm);
        session.hasAlarm = true;
        appRepository.notifyAlarmsChanged();
    }

    public static long setUpdateAlarm(Context context, boolean initial) {
        final AlarmManager alarmManager = Contexts.getAlarmManager(context);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(AlarmReceiver.ALARM_UPDATE);
        final PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        MyApp.LogDebug(LOG_TAG, "set update alarm");
        final long now = Moment.now().toMilliseconds();

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
