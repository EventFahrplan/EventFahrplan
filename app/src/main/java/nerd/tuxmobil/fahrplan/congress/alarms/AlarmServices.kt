package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.app.AlarmManagerCompat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.commons.PendingIntentDelegate
import nerd.tuxmobil.fahrplan.congress.commons.PendingIntentProvider
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

/**
 * Alarm related actions such as adding and deleting session alarms or directly scheduling and
 * discarding alarms via the [AlarmManager][alarmManager].
 */
class AlarmServices @VisibleForTesting constructor(

        private val context: Context,
        private val repository: AppRepository,
        private val alarmManager: AlarmManager,
        private val logging: Logging,
        private val pendingIntentDelegate: PendingIntentDelegate,
) :
    PendingIntentDelegate by pendingIntentDelegate {

    companion object {
        private const val LOG_TAG = "AlarmServices"

        /**
         * Factory function returning an [AlarmServices] instance with sensible defaults set.
         */
        fun newInstance(
            context: Context,
            repository: AppRepository,
            logging: Logging = Logging.get()
        ): AlarmServices {
            val alarmManager = context.getAlarmManager()
            return AlarmServices(
                context = context,
                repository = repository,
                alarmManager = alarmManager,
                logging = logging,
                pendingIntentDelegate = PendingIntentProvider,
            )
        }
    }

    /**
     * Adds an alarm for the given [session] with the given alarm time offset.
     */
    fun addSessionAlarm(session: Session, alarmTimeOffset: Int) {
        logging.d(LOG_TAG, "Add alarm for session = ${session.sessionId}, alarmTimeOffset = $alarmTimeOffset.")
        val sessionStartTime = session.startsAt.toMilliseconds()
        val alarmTimeOffsetMs = alarmTimeOffset * Moment.MILLISECONDS_OF_ONE_MINUTE.toLong()
        val alarmTime = sessionStartTime - alarmTimeOffsetMs
        val moment = Moment.ofEpochMilli(alarmTime)
        logging.d(LOG_TAG, "Add alarm: Time = ${moment.toUtcDateTime()}, in seconds = $alarmTime.")
        val sessionId = session.sessionId
        val sessionTitle = session.title
        val dayIndex = session.dayIndex
        val alarm = Alarm(
            dayIndex = dayIndex,
            sessionId = sessionId,
            sessionTitle = sessionTitle,
            startTime = moment,
        )
        val schedulableAlarm = alarm.toSchedulableAlarm()
        scheduleSessionAlarm(schedulableAlarm, true)
        repository.updateAlarm(alarm)
    }

    /**
     * Deletes the alarm for the given [session].
     */
    fun deleteSessionAlarm(session: Session) {
        val sessionId = session.sessionId
        val alarms = repository.readAlarms(sessionId)
        if (alarms.isNotEmpty()) {
            // Delete any previous alarms of this session.
            val alarm = alarms[0]
            val schedulableAlarm = alarm.toSchedulableAlarm()
            discardSessionAlarm(schedulableAlarm)
            repository.deleteAlarmForSessionId(sessionId)
        }
    }

    /**
     * Returns `true` if the app is allowed to schedule exact alarms. Otherwise `false`.
     * Automatically allowed before Android 12, SnowCone (API level 31) but might be withdrawn
     * by the user via the system settings.
     *
     * See: [AlarmManager.canScheduleExactAlarms].
     */
    val canScheduleExactAlarms: Boolean
        get() = AlarmManagerCompat.canScheduleExactAlarms(alarmManager)

    /**
     * Schedules the given [alarm] via the [AlarmManager].
     * Existing alarms for the associated session are discarded if configured via [discardExisting].
     */
    fun scheduleSessionAlarm(alarm: SchedulableAlarm, discardExisting: Boolean = false) {
        val intent = AlarmReceiver.AlarmIntentFactory(
            context = context,
            sessionId = alarm.sessionId,
            title = alarm.sessionTitle,
            dayIndex = alarm.dayIndex,
            startTime = alarm.startTime
        ).getIntent(isAddAlarmIntent = true)

        val pendingIntent = pendingIntentDelegate.getPendingIntentBroadcast(context, intent)
        if (discardExisting) {
            alarmManager.cancel(pendingIntent)
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            alarm.startTime.toMilliseconds(),
            pendingIntent
        )
    }

    /**
     * Discards the given [alarm] via the [AlarmManager].
     */
    fun discardSessionAlarm(alarm: SchedulableAlarm) {
        val intent = AlarmReceiver.AlarmIntentFactory(
            context = context,
            sessionId = alarm.sessionId,
            title = alarm.sessionTitle,
            dayIndex = alarm.dayIndex,
            startTime = alarm.startTime
        ).getIntent(isAddAlarmIntent = false)
        discardAlarm(context, intent)
    }

    /**
     * Discards an internal alarm used for automatic schedule updates via the [AlarmManager].
     */
    fun discardAutoUpdateAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.ALARM_UPDATE
        discardAlarm(context, intent)
    }

    private fun discardAlarm(context: Context, intent: Intent) {
        val pendingIntent = pendingIntentDelegate.getPendingIntentBroadcast(context, intent)
        alarmManager.cancel(pendingIntent)
    }

}
