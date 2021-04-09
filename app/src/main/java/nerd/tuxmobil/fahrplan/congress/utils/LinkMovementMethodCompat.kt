package nerd.tuxmobil.fahrplan.congress.utils

import android.text.method.MovementMethod
import me.saket.bettermovementmethod.BetterLinkMovementMethod

object LinkMovementMethodCompat {

    /**
     * Returns an instance of [BetterLinkMovementMethod].
     */
    @JvmStatic
    fun getInstance(): MovementMethod = BetterLinkMovementMethod.getInstance()

}
