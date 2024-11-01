package nerd.tuxmobil.fahrplan.congress.commons

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES

/**
 * Delegate which provides a [PendingIntent].
 */
object PendingIntentProvider : PendingIntentDelegate {

    /**
     * Flag indicating that the created PendingIntent should be immutable.
     * See [PendingIntent.FLAG_IMMUTABLE].
     */
    private val FLAG_IMMUTABLE = if (SDK_INT < VERSION_CODES.M) 0 else PendingIntent.FLAG_IMMUTABLE

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
