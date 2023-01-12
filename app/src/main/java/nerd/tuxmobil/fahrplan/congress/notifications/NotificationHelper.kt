package nerd.tuxmobil.fahrplan.congress.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.getNotificationManager

internal class NotificationHelper(context: Context) : ContextWrapper(context) {

    private val notificationManager: NotificationManagerCompat by lazy {
        getNotificationManager()
    }

    val notificationsEnabled = notificationManager.areNotificationsEnabled()

    init {
        createChannels()
    }

    fun createChannels(){
        createNotificationChannel(SESSION_ALARM_CHANNEL_ID, sessionAlarmChannelName, sessionAlarmChannelDescription)
        createNotificationChannel(SCHEDULE_UPDATE_CHANNEL_ID, scheduleUpdateChannelName, scheduleUpdateChannelDescription)
    }

    fun getSessionAlarmNotificationBuilder(
            contentIntent: PendingIntent,
            contentTitle: String,
            occurredAt: Long,
            sound: Uri?,
            deleteIntent: PendingIntent
    ): NotificationCompat.Builder =
            getNotificationBuilder(SESSION_ALARM_CHANNEL_ID, contentIntent, sessionAlarmContentText, sound)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentTitle(contentTitle)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                    .setWhen(occurredAt)
                    .setDeleteIntent(deleteIntent)

    fun getScheduleUpdateNotificationBuilder(
            contentIntent: PendingIntent,
            contentText: String,
            changesCount: Int,
            sound: Uri?
    ): NotificationCompat.Builder =
            getNotificationBuilder(SCHEDULE_UPDATE_CHANNEL_ID, contentIntent, contentText, sound)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentTitle(sessionAlarmContentTitle)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setSubText(getScheduleUpdateContentText(changesCount))

    @JvmOverloads
    fun notify(id: Int, builder: NotificationCompat.Builder, isInsistent: Boolean = false) {
        val notification = builder.build()
        if (isInsistent) {
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
        }
        notificationManager.notify(id, notification)
    }

    /**
     * Cancels all existing schedule update notifications.
     */
    fun cancelScheduleUpdateNotification() {
        notificationManager.cancel(SCHEDULE_UPDATE_ID)
    }

    private fun createNotificationChannel(id: String, name: String, descriptionText: String) {
        val channel = NotificationChannelCompat
            .Builder(id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(name)
            .setDescription(descriptionText)
            .setLightColor(color)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    private fun getNotificationBuilder(
            channelId: String,
            contentIntent: PendingIntent,
            contentText: String,
            sound: Uri?) =
            NotificationCompat.Builder(this, channelId)
                    .setAutoCancel(true)
                    .setColor(color)
                    .setContentIntent(contentIntent)
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(smallIcon)
                    .setSound(sound)

    private val sessionAlarmChannelDescription: String
        get() = getString(R.string.notifications_session_alarm_channel_description)

    private val sessionAlarmChannelName: String
        get() = getString(R.string.notifications_session_alarm_channel_name)

    private val sessionAlarmContentTitle: String
        get() = getString(R.string.notifications_session_alarm_content_title)

    private val sessionAlarmContentText: String
        get() = getString(R.string.notifications_session_alarm_content_text)

    private val scheduleUpdateChannelDescription: String
        get() = getString(R.string.notifications_schedule_update_channel_description)

    private val scheduleUpdateChannelName: String
        get() = getString(R.string.notifications_schedule_update_channel_name)

    private fun getScheduleUpdateContentText(changesCount: Int): String =
            resources.getQuantityString(R.plurals.notifications_schedule_changes, changesCount, changesCount)

    private val color: Int
        get() = ContextCompat.getColor(this, R.color.colorAccent)

    private val smallIcon: Int
        get() = R.drawable.ic_notification

    companion object {
        private const val SESSION_ALARM_CHANNEL_ID = "SESSION_ALARM_CHANNEL"
        private const val SCHEDULE_UPDATE_CHANNEL_ID = "SCHEDULE_UPDATE_CHANNEL"
        const val SCHEDULE_UPDATE_ID = 2
    }
}
