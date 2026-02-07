@file:JvmName("Contexts")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.content.res.Configuration
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
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

/**
 * Returns the package name for the default browsable app.
 * Here, intentionally a website which is unrelated to the domain of the event is passed to avoid
 * returning our own package name which can be registered as an app links handler.
 */
fun Context.getDefaultBrowsableApp(): String? {
    val uri = "https://eventfahrplan.eu".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    val flags = if (SDK_INT < M) 0 else MATCH_DEFAULT_ONLY
    return packageManager
        .resolveActivity(intent, flags)
        ?.activityInfo
        ?.packageName
}

/**
 * Returns a list of package names for all browser apps.
 * Note [Intent.CATEGORY_APP_BROWSER] is used here compared
 * to [getDefaultBrowsableApp].
 */
fun Context.getBrowserApps(): List<String> {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_BROWSER)
    }
    val flags = if (SDK_INT < M) 0 else MATCH_ALL
    return packageManager
        .queryIntentActivities(intent, flags)
        .map { it.activityInfo.packageName }
}

fun Context.openLinkWithApp(link: String, packageName: String) {
    val intent = Intent(Intent.ACTION_VIEW, link.toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        setPackage(packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent) {
        showToast(R.string.share_error_activity_not_found, showShort = true)
    }
}

fun Context.openLink(link: String) {
    val intent = Intent(Intent.ACTION_VIEW, link.toUri())
    intent.addCategory(Intent.CATEGORY_BROWSABLE)   // required
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val chooser = Intent.createChooser(intent, "Open with")
    startActivity(chooser)
}

fun Context.isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.openMap(locationText: String) {
    val encodedLocationText = Uri.encode(locationText)
    val uri = "geo:0,0?q=$encodedLocationText".toUri()
    startActivity(Intent(Intent.ACTION_VIEW).apply { data = uri }) {
        showToast(R.string.share_error_activity_not_found, showShort = true)
    }
}

fun Context.showToast(@StringRes message: Int, showShort: Boolean) {
    val duration = if (showShort) LENGTH_SHORT else LENGTH_LONG
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(message: String, showShort: Boolean) {
    val duration = if (showShort) LENGTH_SHORT else LENGTH_LONG
    Toast.makeText(this, message, duration).show()
}
