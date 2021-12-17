package nerd.tuxmobil.fahrplan.congress.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.DateInfo;
import nerd.tuxmobil.fahrplan.congress.models.DateInfos;


public class FahrplanMisc {

    private static final String LOG_TAG = "FahrplanMisc";

    /**
     * Returns a {@link DateInfos} instance composed from the given {@code dateInfos} list.
     * Duplicate {@link DateInfo} entries are removed.
     */
    @NonNull
    public static DateInfos createDateInfos(@NonNull List<DateInfo> dateInfos) {
        DateInfos infos = new DateInfos();
        for (DateInfo dateInfo : dateInfos) {
            if (!infos.contains(dateInfo)) {
                infos.add(dateInfo);
            }
        }
        for (DateInfo dateInfo : infos) {
            MyApp.LogDebug(LOG_TAG, "DateInfo: " + dateInfo);
        }
        return infos;
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
                    public void onCancelUpdateAlarm() {
                        MyApp.LogDebug(LOG_TAG, "Canceling alarm.");
                        alarmManager.cancel(pendingintent);
                    }

                    @Override
                    public void onScheduleUpdateAlarm(long interval, long nextFetch) {
                        MyApp.LogDebug(LOG_TAG, "Scheduling update alarm to interval " + interval +
                                ", next in ~" + (nextFetch - now));
                        alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP, nextFetch, interval, pendingintent);
                    }

                });
        return alarmUpdater.calculateInterval(now, initial);
    }

}
