package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting

import androidx.core.net.toUri

import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver.AlarmIntentFactory.Companion.ALARM_SESSION

import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity.Companion.createLaunchIntent
import nerd.tuxmobil.fahrplan.congress.utils.PendingIntentCompat.FLAG_IMMUTABLE

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ALARM_UPDATE = "nerd.tuxmobil.fahrplan.congress.ALARM_UPDATE"
        private const val ALARM_DISMISSED = "nerd.tuxmobil.fahrplan.congress.ALARM_DISMISSED"
        private const val LOG_TAG = "AlarmReceiver"
        private const val BUNDLE_KEY_NOTIFICATION_ID = "BUNDLE_KEY_NOTIFICATION_ID"
        private const val INVALID_NOTIFICATION_ID = -1
        private const val DEFAULT_REQUEST_CODE = 0

        /**
         * Returns a unique [Intent] to delete the data associated with the
         * given session alarm [notificationId].
         * The [Intent] is composed with a fake data URI to comply with the uniqueness rules
         * defined by [Intent.filterEquals].
         */
        private fun createDeleteNotificationIntent(context: Context, notificationId: Int) =
            Intent(context, AlarmReceiver::class.java).apply {
                action = ALARM_DISMISSED
                putExtra(BUNDLE_KEY_NOTIFICATION_ID, notificationId)
                data = "fake://$notificationId".toUri()
            }
    }

    private val logging = Logging.get()

    override fun onReceive(context: Context, intent: Intent) {
        logging.d(LOG_TAG, "Received alarm = ${intent.action}.")

        when (intent.action) {
            ALARM_DISMISSED -> onSessionAlarmNotificationDismissed(intent)
            ALARM_UPDATE -> UpdateService.start(context)
            ALARM_SESSION -> {
                val sessionId = intent.getStringExtra(BundleKeys.ALARM_SESSION_ID)!!
                val day = intent.getIntExtra(BundleKeys.ALARM_DAY, 1)
                val start = intent.getLongExtra(BundleKeys.ALARM_START_TIME, System.currentTimeMillis())
                val title = intent.getStringExtra(BundleKeys.ALARM_TITLE)!!
                logging.report(LOG_TAG, "sessionId = $sessionId, intent = $intent")
                //Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();

                val uniqueNotificationId = AppRepository.createSessionAlarmNotificationId(sessionId)
                val launchIntent = createLaunchIntent(context, sessionId, day, uniqueNotificationId)
                val contentIntent = PendingIntent.getActivity(
                    context,
                    DEFAULT_REQUEST_CODE,
                    launchIntent,
                    FLAG_ONE_SHOT or FLAG_IMMUTABLE
                )

                val notificationHelper = NotificationHelper(context)
                val soundUri = AppRepository.readAlarmToneUri()

                val deleteNotificationIntent = createDeleteNotificationIntent(context, uniqueNotificationId)
                val deleteBroadcastIntent = PendingIntent.getBroadcast(
                    context,
                    DEFAULT_REQUEST_CODE,
                    deleteNotificationIntent,
                    FLAG_IMMUTABLE
                )

                val builder = notificationHelper.getSessionAlarmNotificationBuilder(
                    contentIntent, title, start, soundUri, deleteBroadcastIntent
                )
                val isInsistentAlarmsEnabled = AppRepository.readInsistentAlarmsEnabled()
                notificationHelper.notify(uniqueNotificationId, builder, isInsistentAlarmsEnabled)

                AppRepository.deleteAlarmForSessionId(sessionId)
            }
        }
    }

    private fun onSessionAlarmNotificationDismissed(intent: Intent) {
        val notificationId = intent.getIntExtra(BUNDLE_KEY_NOTIFICATION_ID, INVALID_NOTIFICATION_ID)
        check(notificationId != INVALID_NOTIFICATION_ID) {
            "Bundle does not contain NOTIFICATION_ID."
        }
        AppRepository.deleteSessionAlarmNotificationId(notificationId)
    }

    internal class AlarmIntentFactory(
        val context: Context,
        val sessionId: String,
        val title: String,
        val day: Int,
        val startTime: Long,
    ) {

        fun getIntent(isAddAlarmIntent: Boolean) = Intent(context, AlarmReceiver::class.java)
            .withExtras(
                BundleKeys.ALARM_SESSION_ID to sessionId,
                BundleKeys.ALARM_DAY to day,
                BundleKeys.ALARM_TITLE to title,
                BundleKeys.ALARM_START_TIME to startTime
            ).apply {
                action = if (isAddAlarmIntent) ALARM_SESSION else ALARM_DELETE
                data = "alarm://$sessionId".toUri()
            }

        companion object {
            @VisibleForTesting
            const val ALARM_SESSION = "nerd.tuxmobil.fahrplan.congress.ALARM_SESSION"

            @VisibleForTesting
            const val ALARM_DELETE = "de.machtnix.fahrplan.ALARM"
        }

    }

}
