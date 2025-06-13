package nerd.tuxmobil.fahrplan.congress.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater
import nerd.tuxmobil.fahrplan.congress.commons.PendingIntentProvider
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.NextFetch


object FahrplanMisc {

    private const val LOG_TAG = "FahrplanMisc"

    /**
     * Returns a [DateInfos] instance composed from the given [dateInfos] list.
     * Duplicate [DateInfo] entries are removed.
     */
    fun createDateInfos(dateInfos: List<DateInfo>): DateInfos {
        val infos = DateInfos()
        for (dateInfo in dateInfos) {
            if (dateInfo !in infos) {
                infos.add(dateInfo)
            }
        }
        return infos
    }

    fun setUpdateAlarm(
        context: Context,
        conferenceTimeFrame: ConferenceTimeFrame,
        isInitial: Boolean,
        logging: Logging,
        onCancelScheduleNextFetch: () -> Unit,
        onUpdateScheduleNextFetch: (NextFetch) -> Unit,
    ): Long {
        val alarmManager = context.getAlarmManager()
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
            .apply { action = AlarmReceiver.ALARM_UPDATE }
        val pendingIntent = PendingIntentProvider.getPendingIntentBroadcast(context, alarmIntent)

        logging.d(LOG_TAG, "set update alarm")
        val now = Moment.now()

        return AlarmUpdater(conferenceTimeFrame, object : AlarmUpdater.OnAlarmUpdateListener {

            override fun onCancelUpdateAlarm() {
                logging.d(LOG_TAG, "Canceling alarm.")
                onCancelScheduleNextFetch()
                alarmManager.cancel(pendingIntent)
            }

            override fun onScheduleUpdateAlarm(interval: Long, nextFetch: Moment) {
                val next = nextFetch.minusMilliseconds(now.toMilliseconds()).toMilliseconds()
                val nextDateTime = DateFormatter.newInstance(useDeviceTimeZone = false)
                    .getFormattedDateTimeLong(nextFetch, sessionZoneOffset = null)
                logging.d(LOG_TAG, "Scheduling update alarm to interval $interval, next in ~$next ms, at $nextDateTime")
                // Redesign might be needed as of Android 12 (API level 31)
                // See https://developer.android.com/training/scheduling/alarms
                onUpdateScheduleNextFetch(NextFetch(nextFetch, Duration.ofMilliseconds(interval)))
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextFetch.toMilliseconds(), interval, pendingIntent)
            }

        }).calculateInterval(now, isInitial)
    }

}
