package nerd.tuxmobil.fahrplan.congress.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos


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
        for (dateInfo in infos) {
            MyApp.LogDebug(LOG_TAG, "DateInfo: $dateInfo")
        }
        return infos
    }

    @JvmStatic
    fun setUpdateAlarm(context: Context, isInitial: Boolean): Long {
        val alarmManager = context.getAlarmManager()
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
            .apply { action = AlarmReceiver.ALARM_UPDATE }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

        MyApp.LogDebug(LOG_TAG, "set update alarm")
        val now = Moment.now().toMilliseconds()

        return AlarmUpdater(MyApp.conferenceTimeFrame, object : AlarmUpdater.OnAlarmUpdateListener {

            override fun onCancelUpdateAlarm() {
                MyApp.LogDebug(LOG_TAG, "Canceling alarm.")
                alarmManager.cancel(pendingIntent)
            }

            override fun onScheduleUpdateAlarm(interval: Long, nextFetch: Long) {
                MyApp.LogDebug(LOG_TAG, "Scheduling update alarm to interval $interval, next in ~${nextFetch - now}")
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextFetch, interval, pendingIntent)
            }

        }).calculateInterval(now, isInitial)
    }

}
