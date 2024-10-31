package nerd.tuxmobil.fahrplan.congress.commons

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Delegate to get a [PendingIntent].
 */
interface PendingIntentDelegate {

    fun getPendingIntentActivity(context: Context, intent: Intent): PendingIntent

    fun getPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent

}
