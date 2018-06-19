@file:JvmName("AlarmServices")

package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import nerd.tuxmobil.fahrplan.congress.extensions.getAlarmManager
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm

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
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
    val alarmManager = context.getAlarmManager()
    if (discardExisting) {
        alarmManager.cancel(pendingIntent)
    }
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
}

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

fun discardAutoUpdateAlarm(context: Context) {
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = AlarmReceiver.ALARM_UPDATE
    discardAlarm(context, 0, intent)
}

private fun discardAlarm(context: Context, requestCode: Int, intent: Intent) {
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
    context.getAlarmManager().cancel(pendingIntent)
}
