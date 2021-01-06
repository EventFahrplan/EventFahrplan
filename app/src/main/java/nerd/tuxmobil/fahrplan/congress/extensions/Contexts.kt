@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import androidx.core.content.getSystemService

fun Context.getAlarmManager() = getSystemService<AlarmManager>()!!

fun Context.getLayoutInflater() = getSystemService<LayoutInflater>()!!

fun Context.getNotificationManager() = getSystemService<NotificationManager>()!!

fun Context.startActivity(intent: Intent, onActivityNotFound: () -> Unit) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onActivityNotFound.invoke()
    }
}

fun Context.isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
