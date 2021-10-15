package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame

class AlarmUpdater @JvmOverloads constructor(

    private val conference: ConferenceTimeFrame,
    private val listener: OnAlarmUpdateListener,
    private val logging: Logging = Logging.get()

) {

    interface OnAlarmUpdateListener {
        fun onCancelAlarm()
        fun onRescheduleAlarm(interval: Long, nextFetch: Long)
        fun onRescheduleInitialAlarm(interval: Long, nextFetch: Long)
    }

    companion object {

        private const val LOG_TAG = "AlarmUpdater"
        private const val TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR
        private const val ONE_DAY = AlarmManager.INTERVAL_DAY
    }

    fun calculateInterval(time: Long, isInitial: Boolean): Long {
        var interval: Long
        var nextFetch: Long
        when {
            conference.contains(time) -> {
                logging.d(LOG_TAG, "START <= time < END");
                interval = TWO_HOURS
                nextFetch = time + interval
            }
            conference.endsBefore(time) -> {
                logging.d(LOG_TAG, "START < END <= time");
                listener.onCancelAlarm()
                return 0
            }
            else -> {
                logging.d(LOG_TAG, "time < END");
                interval = ONE_DAY
                nextFetch = time + interval
            }
        }
        val shiftedTime = time + ONE_DAY
        if (conference.startsAfter(time) && conference.startsAtOrBefore(shiftedTime)) {
            logging.d(LOG_TAG, "time < START && START <= shiftedTime");
            interval = TWO_HOURS
            nextFetch = conference.firstDayStartTime
            if (!isInitial) {
                listener.onCancelAlarm()
                listener.onRescheduleAlarm(interval, nextFetch)
            }
        }
        if (isInitial) {
            listener.onCancelAlarm()
            listener.onRescheduleInitialAlarm(interval, nextFetch)
        }
        return interval
    }

}
