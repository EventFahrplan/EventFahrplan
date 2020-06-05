@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.core.content.ContextCompat

fun Context.getAlarmManager() = ContextCompat.getSystemService(this, AlarmManager::class.java)!!

fun Context.getLayoutInflater() = ContextCompat.getSystemService(this, LayoutInflater::class.java)!!

fun Context.getNotificationManager() = ContextCompat.getSystemService(this, NotificationManager::class.java)!!

fun Context.startActivity(intent: Intent, onActivityNotFound: () -> Unit) {
    if (intent.resolveActivity(packageManager) == null) {
        onActivityNotFound.invoke()
    } else {
        startActivity(intent)
    }
}
