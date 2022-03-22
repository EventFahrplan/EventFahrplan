package nerd.tuxmobil.fahrplan.congress.utils

import android.app.PendingIntent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES

object PendingIntentCompat {

    /**
     * Flag indicating that the created PendingIntent should be immutable.
     * See [PendingIntent.FLAG_IMMUTABLE].
     */
    @JvmField
    val FLAG_IMMUTABLE = if (SDK_INT >= VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

}
