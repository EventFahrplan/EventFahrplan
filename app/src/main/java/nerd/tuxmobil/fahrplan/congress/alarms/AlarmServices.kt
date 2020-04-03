package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm

typealias PendingIntentCallback = (Context, Int, Intent, Int) -> PendingIntent

/**
 * Alarm related actions such as scheduling and discarding alarms via the [AlarmManager][alarmManager].
 */
class AlarmServices @JvmOverloads constructor(

        private val alarmManager: AlarmManager,
        private val onPendingIntentBroadcast: PendingIntentCallback = { context, requestCode, intent, flags ->
            PendingIntent.getBroadcast(context, requestCode, intent, flags)
        }

) {

    /**
     * Schedules the given [alarm] via the [android.app.AlarmManager].
     * Existing alarms for the associated event are discarded if configured via [discardExisting].
     */
    @JvmOverloads
    fun scheduleEventAlarm(context: Context, alarm: SchedulableAlarm, discardExisting: Boolean = false) {
        val intent = AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setLectureId(alarm.eventId)
                .setDay(alarm.day)
                .setTitle(alarm.eventTitle)
                .setStartTime(alarm.startTime)
                .setIsAddAlarm()
                .build()

        val requestCode = Integer.parseInt(alarm.eventId)
        val pendingIntent = onPendingIntentBroadcast(context, requestCode, intent, 0)
        if (discardExisting) {
            alarmManager.cancel(pendingIntent)
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    /**
     * Discards the given [alarm] via the [android.app.AlarmManager].
     */
    fun discardEventAlarm(context: Context, alarm: SchedulableAlarm) {
        val intent = AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setLectureId(alarm.eventId)
                .setDay(alarm.day)
                .setTitle(alarm.eventTitle)
                .setStartTime(alarm.startTime)
                .setIsDeleteAlarm()
                .build()

        val requestCode = Integer.parseInt(alarm.eventId)
        discardAlarm(context, requestCode, intent)
    }

    /**
     * Discards an internal alarm used for automatic schedule updates via the [android.app.AlarmManager].
     */
    fun discardAutoUpdateAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.ALARM_UPDATE
        discardAlarm(context, 0, intent)
    }

    private fun discardAlarm(context: Context, requestCode: Int, intent: Intent) {
        val pendingIntent = onPendingIntentBroadcast(context, requestCode, intent, 0)
        alarmManager.cancel(pendingIntent)
    }

}
