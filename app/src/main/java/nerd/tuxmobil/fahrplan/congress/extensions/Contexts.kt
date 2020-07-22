@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.core.content.getSystemService

fun Context.getAlarmManager() = getSystemService<AlarmManager>()!!

fun Context.getLayoutInflater() = getSystemService<LayoutInflater>()!!

fun Context.getNotificationManager() = getSystemService<NotificationManager>()!!

fun Context.startActivity(intent: Intent, onActivityNotFound: () -> Unit) {
    if (intent.resolveActivity(packageManager) == null) {
        onActivityNotFound.invoke()
    } else {
        startActivity(intent)
    }
}
