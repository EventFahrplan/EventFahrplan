package nerd.tuxmobil.fahrplan.congress.alarms;

import android.app.AlarmManager;
import android.support.annotation.NonNull;

import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame;

public class AlarmUpdater {

    public interface OnAlarmUpdateListener {

        void onCancelAlarm();

        void onRescheduleAlarm(long interval, long nextFetch);

        void onRescheduleInitialAlarm(long interval, long nextFetch);

    }

    private final ConferenceTimeFrame conference;

    private final OnAlarmUpdateListener listener;

    public AlarmUpdater(@NonNull ConferenceTimeFrame conferenceTimeFrame,
                        @NonNull OnAlarmUpdateListener listener) {
        this.conference = conferenceTimeFrame;
        this.listener = listener;
    }

    public long calculateInterval(long time, boolean initial) {
        long interval;
        long nextFetch;

        long TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR;
        long ONE_DAY = AlarmManager.INTERVAL_DAY;

        if (conference.contains(time)) {
            interval = TWO_HOURS;
            nextFetch = time + interval;
        } else if (conference.endsBefore(time)) {
            listener.onCancelAlarm();
            return 0;
        } else {
            interval = ONE_DAY;
            nextFetch = time + interval;
        }

        long shiftedTime = time + ONE_DAY;
        if (conference.startsAfter(time) && conference.startsAtOrBefore(shiftedTime)) {
            interval = TWO_HOURS;
            nextFetch = conference.getFirstDayStartTime();
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
