@file:JvmName("Engagements")

package nerd.tuxmobil.fahrplan.congress.engagements

import android.support.v7.app.AppCompatActivity
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.navigation.C3navSnack
import org.ligi.snackengage.SnackEngage

private const val VENUE_LEIPZIG_MESSE = "leipzig-messe"

@Suppress("ConstantConditionIf")
fun AppCompatActivity.initUserEngagement() {
    val snackEngageBuilder = SnackEngage.from(this)
    if (BuildConfig.ENGAGE_GOOGLE_PLAY_RATING) {
        snackEngageBuilder.withSnack(RateSnack(this))
    }
    if (VENUE_LEIPZIG_MESSE == BuildConfig.VENUE) {
        snackEngageBuilder.withSnack(C3navSnack(this))
    }
    snackEngageBuilder
            .build()
            .engageWhenAppropriate()
}
