package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm

typealias PendingIntentCallback = (Context, Intent) -> PendingIntent

/**
 * Alarm related actions such as scheduling and discarding alarms via the [AlarmManager][alarmManager].
 */
class AlarmServices @JvmOverloads constructor(

        private val alarmManager: AlarmManager,
        private val onPendingIntentBroadcast: PendingIntentCallback = { context, intent ->
            PendingIntent.getBroadcast(context, DEFAULT_REQUEST_CODE, intent, NO_FLAGS)
        }

) {

    private companion object {
        const val DEFAULT_REQUEST_CODE = 0
        const val NO_FLAGS = 0
    }

    /**
     * Schedules the given [alarm] via the [AlarmManager].
     * Existing alarms for the associated session are discarded if configured via [discardExisting].
     */
    @JvmOverloads
    fun scheduleSessionAlarm(context: Context, alarm: SchedulableAlarm, discardExisting: Boolean = false) {
        val intent = AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setSessionId(alarm.sessionId)
                .setDay(alarm.day)
                .setTitle(alarm.sessionTitle)
                .setStartTime(alarm.startTime)
                .setIsAddAlarm()
                .build()

        val pendingIntent = onPendingIntentBroadcast(context, intent)
        if (discardExisting) {
            alarmManager.cancel(pendingIntent)
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    /**
     * Discards the given [alarm] via the [AlarmManager].
     */
    fun discardSessionAlarm(context: Context, alarm: SchedulableAlarm) {
        val intent = AlarmReceiver.AlarmIntentBuilder()
                .setContext(context)
                .setSessionId(alarm.sessionId)
                .setDay(alarm.day)
                .setTitle(alarm.sessionTitle)
                .setStartTime(alarm.startTime)
                .setIsDeleteAlarm()
                .build()
        discardAlarm(context, intent)
    }

    /**
     * Discards an internal alarm used for automatic schedule updates via the [AlarmManager].
     */
    fun discardAutoUpdateAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.ALARM_UPDATE
        discardAlarm(context, intent)
    }

    private fun discardAlarm(context: Context, intent: Intent) {
        val pendingIntent = onPendingIntentBroadcast(context, intent)
        alarmManager.cancel(pendingIntent)
    }

}
