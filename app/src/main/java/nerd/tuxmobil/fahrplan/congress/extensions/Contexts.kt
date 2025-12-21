@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import nerd.tuxmobil.fahrplan.congress.R

fun Context.getAlarmManager() = getSystemService<AlarmManager>()!!

fun Context.getLayoutInflater() = getSystemService<LayoutInflater>()!!

fun Context.getNotificationManager() = NotificationManagerCompat.from(this)

fun Context.startActivity(intent: Intent, onActivityNotFound: () -> Unit) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        onActivityNotFound.invoke()
    }
}

fun Context.isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.openMap(locationText: String) {
    val encodedLocationText = Uri.encode(locationText)
    val uri = "geo:0,0?q=$encodedLocationText".toUri()
    startActivity(Intent(Intent.ACTION_VIEW).apply { data = uri }) {
        Toast.makeText(this, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
    }
}
