package nerd.tuxmobil.fahrplan.congress.engagements

import android.content.Context
import nerd.tuxmobil.fahrplan.congress.R
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce
import org.ligi.snackengage.snacks.BaseSnack

/**
 * Snack to engage that the user rotates the device to learn
 * that in landscape mode there is much more screen estate.
 */
class LandscapeOrientationSnack(

    val context: Context

) : BaseSnack() {

    init {
        withConditions(
            NeverAgainWhenClickedOnce(),
            AfterNumberOfOpportunities(2),
        )
    }

    override fun getId() = "LANDSCAPE_ORIENTATION"

    override fun getText() =
        context.getString(R.string.snack_engage_landscape_orientation_title)

    override fun getActionText() =
        context.getString(R.string.snack_engage_landscape_orientation_action)

}
