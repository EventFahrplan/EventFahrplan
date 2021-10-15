package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame

class AlarmUpdater(

    private val conference: ConferenceTimeFrame,
    private val listener: OnAlarmUpdateListener

) {

    interface OnAlarmUpdateListener {
        fun onCancelAlarm()
        fun onRescheduleAlarm(interval: Long, nextFetch: Long)
        fun onRescheduleInitialAlarm(interval: Long, nextFetch: Long)
    }

    companion object {

        private const val TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR
        private const val ONE_DAY = AlarmManager.INTERVAL_DAY
    }

    fun calculateInterval(time: Long, isInitial: Boolean): Long {
        var interval: Long
        var nextFetch: Long
        when {
            conference.contains(time) -> {
                interval = TWO_HOURS
                nextFetch = time + interval
            }
            conference.endsBefore(time) -> {
                listener.onCancelAlarm()
                return 0
            }
            else -> {
                interval = ONE_DAY
                nextFetch = time + interval
            }
        }
        val shiftedTime = time + ONE_DAY
        if (conference.startsAfter(time) && conference.startsAtOrBefore(shiftedTime)) {
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
