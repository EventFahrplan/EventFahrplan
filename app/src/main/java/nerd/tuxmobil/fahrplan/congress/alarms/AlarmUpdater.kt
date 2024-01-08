package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame

class AlarmUpdater @JvmOverloads constructor(

    private val conference: ConferenceTimeFrame,
    private val listener: OnAlarmUpdateListener,
    private val appRepository: AppRepository = AppRepository,
    private val logging: Logging = Logging.get()

) {

    interface OnAlarmUpdateListener {
        fun onCancelUpdateAlarm()
        fun onScheduleUpdateAlarm(interval: Long, nextFetch: Long)
    }

    companion object {

        private const val LOG_TAG = "AlarmUpdater"
        private const val TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR
        private const val ONE_DAY = AlarmManager.INTERVAL_DAY
    }

    fun calculateInterval(time: Long, isInitial: Boolean): Long {
        val refreshIntervalDefaultValue = appRepository.readScheduleRefreshIntervalDefaultValue()
        val refreshInterval = appRepository.readScheduleRefreshInterval()
        val shouldUseDevelopmentInterval = refreshInterval != refreshIntervalDefaultValue
        val developmentRefreshInterval = refreshInterval.toLong()

        if (shouldUseDevelopmentInterval) {
            logging.d(LOG_TAG, "Schedule refresh interval = $developmentRefreshInterval")
            listener.onCancelUpdateAlarm()
            val nextFetch = time + developmentRefreshInterval
            listener.onScheduleUpdateAlarm(developmentRefreshInterval, nextFetch)
            return developmentRefreshInterval
        }

        var interval: Long
        var nextFetch: Long
        when {
            conference.contains(Moment.ofEpochMilli(time)) -> {
                logging.d(LOG_TAG, "START <= time < END")
                interval = TWO_HOURS
                nextFetch = time + interval
            }
            conference.endsAtOrBefore(Moment.ofEpochMilli(time)) -> {
                logging.d(LOG_TAG, "START < END <= time")
                listener.onCancelUpdateAlarm()
                return 0
            }
            else -> {
                logging.d(LOG_TAG, "time < END")
                interval = ONE_DAY
                nextFetch = time + interval
            }
        }
        val shiftedTime = time + ONE_DAY
        if (conference.startsAfter(Moment.ofEpochMilli(time)) && conference.startsAtOrBefore(Moment.ofEpochMilli(shiftedTime))) {
            logging.d(LOG_TAG, "time < START && START <= shiftedTime")
            interval = TWO_HOURS
            nextFetch = conference.firstDayStartTime.toMilliseconds()
            if (!isInitial) {
                listener.onCancelUpdateAlarm()
                listener.onScheduleUpdateAlarm(interval, nextFetch)
            }
        }
        if (isInitial) {
            listener.onCancelUpdateAlarm()
            listener.onScheduleUpdateAlarm(interval, nextFetch)
        }
        return interval
    }

}
