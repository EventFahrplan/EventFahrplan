package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame

class AlarmUpdater(

    private val conference: ConferenceTimeFrame,
    private val listener: OnAlarmUpdateListener,
    private val appRepository: AppRepository = AppRepository,
    private val logging: Logging = Logging.get()

) {

    interface OnAlarmUpdateListener {
        fun onCancelUpdateAlarm()
        fun onScheduleUpdateAlarm(interval: Long, nextFetch: Moment)
    }

    companion object {

        private const val LOG_TAG = "AlarmUpdater"
        private const val TWO_HOURS = 2 * AlarmManager.INTERVAL_HOUR
        private const val ONE_DAY = AlarmManager.INTERVAL_DAY
    }

    fun calculateInterval(moment: Moment, isInitial: Boolean): Long {
        val refreshIntervalDefaultValue = appRepository.readScheduleRefreshIntervalDefaultValue()
        val refreshInterval = appRepository.readScheduleRefreshInterval()
        val shouldUseDevelopmentInterval = refreshInterval != refreshIntervalDefaultValue
        val developmentRefreshInterval = refreshInterval.toLong()

        if (shouldUseDevelopmentInterval) {
            logging.d(LOG_TAG, "Schedule refresh interval = $developmentRefreshInterval")
            listener.onCancelUpdateAlarm()
            val nextFetch = moment.plusMilliseconds(developmentRefreshInterval)
            listener.onScheduleUpdateAlarm(developmentRefreshInterval, nextFetch)
            return developmentRefreshInterval
        }

        var interval: Long
        var nextFetch: Moment
        when {
            conference.contains(moment) -> {
                logging.d(LOG_TAG, "START <= moment < END")
                interval = TWO_HOURS
                nextFetch = moment.plusMilliseconds(interval)
            }
            conference.endsAtOrBefore(moment) -> {
                logging.d(LOG_TAG, "START < END <= moment")
                listener.onCancelUpdateAlarm()
                return 0
            }
            else -> {
                logging.d(LOG_TAG, "moment < END")
                interval = ONE_DAY
                nextFetch = moment.plusMilliseconds(interval)
            }
        }
        val shiftedTime = moment.plusDays(1)
        if (conference.startsAfter(moment) && conference.startsAtOrBefore(shiftedTime)) {
            logging.d(LOG_TAG, "moment < START && START <= shiftedTime")
            interval = TWO_HOURS
            nextFetch = conference.firstDayStartTime
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
