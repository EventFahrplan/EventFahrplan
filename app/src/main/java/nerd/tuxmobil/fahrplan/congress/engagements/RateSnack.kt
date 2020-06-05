package nerd.tuxmobil.fahrplan.congress.engagements

import android.content.Context
import androidx.core.content.ContextCompat
import nerd.tuxmobil.fahrplan.congress.R
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce
import org.ligi.snackengage.conditions.connectivity.IsConnectedViaWiFiOrUnknown
import org.ligi.snackengage.snacks.RateSnack as LibraryRateSnack

class RateSnack(val context: Context) : LibraryRateSnack() {

    init {
        overrideTitleText(context.getString(R.string.snack_engage_rate_title))
        overrideActionText(context.getString(R.string.snack_engage_rate_action))
        withConditions(
                NeverAgainWhenClickedOnce(),
                AfterNumberOfOpportunities(13),
                IsConnectedViaWiFiOrUnknown()
        )
        setActionColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

}
