package nerd.tuxmobil.fahrplan.congress.utils

import android.os.Build
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import me.saket.bettermovementmethod.BetterLinkMovementMethod

object LinkMovementMethodCompat {

    /**
     * Returns an instance of [BetterLinkMovementMethod] on devices running
     * Android 4.1.x Jelly Bean (API 16) or newer. [LinkMovementMethod] is
     * returned for devices running an older Android version.
     */
    @JvmStatic
    fun getInstance(): MovementMethod =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                LinkMovementMethod.getInstance()
            } else {
                BetterLinkMovementMethod.getInstance()
            }

}
