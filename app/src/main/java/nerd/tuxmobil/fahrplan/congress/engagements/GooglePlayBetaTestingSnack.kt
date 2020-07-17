package nerd.tuxmobil.fahrplan.congress.engagements

import android.content.Context
import androidx.core.content.ContextCompat
import nerd.tuxmobil.fahrplan.congress.R
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce
import org.ligi.snackengage.conditions.connectivity.IsConnectedViaWiFiOrUnknown
import org.ligi.snackengage.snacks.GooglePlayOpenBetaTestSnack

class GooglePlayBetaTestingSnack(val context: Context) : GooglePlayOpenBetaTestSnack() {

    init {
        overrideTitleText(context.getString(R.string.snack_engage_google_play_beta_testing_title))
        overrideActionText(context.getString(R.string.snack_engage_google_play_beta_testing_action))
        withConditions(
                NeverAgainWhenClickedOnce(),
                AfterNumberOfOpportunities(21),
                IsConnectedViaWiFiOrUnknown()
        )
        setActionColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

}
