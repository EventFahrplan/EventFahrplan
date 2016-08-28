package nerd.tuxmobil.fahrplan.congress;

import android.app.AlarmManager;
import android.support.annotation.NonNull;

public class AlarmUpdater {

    interface OnAlarmUpdateListener {

        void onCancelAlarm();

        void onRescheduleAlarm(long interval, long nextFetch);

        void onRescheduleInitialAlarm(long interval, long nextFetch);

    }

    private final long firstDayStartTime;

    private final long lastDayEndTime;

    private final OnAlarmUpdateListener listener;

    public AlarmUpdater(long firstDayStartTime, long lastDayEndTime,
                        @NonNull OnAlarmUpdateListener listener) {
        this.firstDayStartTime = firstDayStartTime;
        this.lastDayEndTime = lastDayEndTime;
        this.listener = listener;
    }

    public long calculateInterval(long time, boolean initial) {
        long interval;
        long nextFetch;

        long TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR;
        long ONE_DAY = AlarmManager.INTERVAL_DAY;

        if ((time >= firstDayStartTime) && (time < lastDayEndTime)) {
            interval = TWO_HOURS;
            nextFetch = time + interval;
        } else if (time >= lastDayEndTime) {
            listener.onCancelAlarm();
            return 0;
        } else {
            interval = ONE_DAY;
            nextFetch = time + interval;
        }

        long shiftedTime = time + ONE_DAY;
        if ((time < firstDayStartTime) && (shiftedTime >= firstDayStartTime)) {
            interval = TWO_HOURS;
            nextFetch = firstDayStartTime;
            if (!initial) {
                listener.onCancelAlarm();
                listener.onRescheduleAlarm(interval, nextFetch);
            }
        }

        if (initial) {
            listener.onCancelAlarm();
            listener.onRescheduleInitialAlarm(interval, nextFetch);
        }
        return interval;
    }

}
