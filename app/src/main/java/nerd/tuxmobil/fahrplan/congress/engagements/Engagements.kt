@file:JvmName("Engagements")

package nerd.tuxmobil.fahrplan.congress.engagements

import androidx.appcompat.app.AppCompatActivity
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.navigation.C3navSnack
import org.ligi.snackengage.SnackEngage

@Suppress("ConstantConditionIf")
fun AppCompatActivity.initUserEngagement() {
    val snackEngageBuilder = SnackEngage.from(this)
    if (BuildConfig.ENGAGE_GOOGLE_PLAY_RATING) {
        snackEngageBuilder.withSnack(RateSnack(this))
    }
    if (BuildConfig.ENGAGE_GOOGLE_BETA_TESTING) {
        snackEngageBuilder.withSnack(GooglePlayBetaTestingSnack(this))
    }
    if (BuildConfig.ENGAGE_C3NAV_APP_INSTALLATION) {
        snackEngageBuilder.withSnack(C3navSnack(this))
    }
    snackEngageBuilder
            .build()
            .engageWhenAppropriate()
}
