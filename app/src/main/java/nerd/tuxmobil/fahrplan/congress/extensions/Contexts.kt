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
 * Returns an intent to view a website which is intentionally unrelated to the domain of the event.
 * This avoids matching our own package name which can be registered as an app links handler.
 */
private fun createUnrelatedWebsiteIntent() =
    Intent(Intent.ACTION_VIEW, "https://eventfahrplan.eu".toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }

/**
 * Returns the package name for the default browsable app.
 * If no default is set then the package name of the system resolver activity ("android")
 * is returned. Use [getBrowsableApps] to tell apart an actual app from the resolver.
 */
fun Context.getDefaultBrowsableApp(): String? {
    return packageManager
        .resolveActivity(createUnrelatedWebsiteIntent(), MATCH_DEFAULT_ONLY)
        ?.activityInfo
        ?.packageName
}

/**
 * Returns a list of package names for all apps which can view a website.
 * In contrast to [getBrowserApps] this includes browsers which do not declare
 * [Intent.CATEGORY_APP_BROWSER] such as Firefox Klar but also apps which are no browsers.
 * [MATCH_ALL] is required to not only receive the default browser on newer Android versions.
 */
fun Context.getBrowsableApps(): List<String> {
    return packageManager
        .queryIntentActivities(createUnrelatedWebsiteIntent(), MATCH_ALL)
        .map { it.activityInfo.packageName }
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
    return packageManager
        .queryIntentActivities(intent, MATCH_ALL)
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
    val chooser = Intent.createChooser(intent, getString(R.string.app_chooser_title))
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
