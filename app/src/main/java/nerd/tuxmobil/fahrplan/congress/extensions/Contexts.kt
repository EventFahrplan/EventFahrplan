@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.view.LayoutInflater

fun Context.getAlarmManager() = getSystemService(ALARM_SERVICE) as AlarmManager

fun Context.getLayoutInflater() = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

fun Context.getNotificationManager() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

fun Context.startActivity(intent: Intent, onActivityNotFound: () -> Unit) {
    if (intent.resolveActivity(packageManager) == null) {
        onActivityNotFound.invoke()
    } else {
        startActivity(intent)
    }
}
