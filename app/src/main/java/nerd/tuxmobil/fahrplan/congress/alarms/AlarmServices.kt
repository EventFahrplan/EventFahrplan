@file:JvmName("AlarmServices")

package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager

@JvmOverloads
fun scheduleEventAlarm(context: Context, eventId: String, day: Int, title: String, startTime: Long, alarmTime: Long, discardExisting: Boolean = false) {
    val intent = AlarmReceiver.AlarmIntentBuilder()
            .setContext(context)
            .setLectureId(eventId)
            .setDay(day)
            .setTitle(title)
            .setStartTime(startTime)
            .setIsAddAlarm()
            .build()

    val requestCode = Integer.parseInt(eventId)
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
    val alarmManager = context.getAlarmManager()
    if (discardExisting) {
        alarmManager.cancel(pendingIntent)
    }
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
}

fun discardEventAlarm(context: Context, eventId: String, day: Int, title: String, startTime: Long) {
    val intent = AlarmReceiver.AlarmIntentBuilder()
            .setContext(context)
            .setLectureId(eventId)
            .setDay(day)
            .setTitle(title)
            .setStartTime(startTime)
            .setIsDeleteAlarm()
            .build()

    val requestCode = Integer.parseInt(eventId)
    discardAlarm(context, requestCode, intent)
}

fun discardAutoUpdateAlarm(context: Context) {
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = AlarmReceiver.ALARM_UPDATE
    discardAlarm(context, 0, intent)
}

private fun discardAlarm(context: Context, requestCode: Int, intent: Intent) {
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
    context.getAlarmManager().cancel(pendingIntent)
}
