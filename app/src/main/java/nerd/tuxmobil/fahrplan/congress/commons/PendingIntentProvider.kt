package nerd.tuxmobil.fahrplan.congress.commons

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent

/**
 * Delegate which provides a [PendingIntent].
 */
object PendingIntentProvider : PendingIntentDelegate {

    private const val DEFAULT_REQUEST_CODE = 0

    @SuppressLint("WrongConstant")
    override fun getPendingIntentActivity(context: Context, intent: Intent): PendingIntent {
        return PendingIntent.getActivity(context, DEFAULT_REQUEST_CODE, intent, FLAG_ONE_SHOT or FLAG_IMMUTABLE)
    }

    @SuppressLint("WrongConstant")
    override fun getPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(context, DEFAULT_REQUEST_CODE, intent, FLAG_IMMUTABLE)
    }

}
