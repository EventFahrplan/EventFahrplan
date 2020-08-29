package nerd.tuxmobil.fahrplan.congress.navigation

import android.content.Context
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import org.ligi.snackengage.SnackContext
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce
import org.ligi.snackengage.conditions.connectivity.IsConnectedViaWiFiOrUnknown
import org.ligi.snackengage.snacks.OpenURLSnack

class C3navSnack(private val context: Context) : OpenURLSnack(

        context.getString(R.string.snack_engage_c3nav_url),
        context.getString(R.string.snack_engage_c3nav_unique_id)

) {

    init {
        val conferenceName = context.getString(R.string.conference_name)
        overrideTitleText(context.getString(R.string.snack_engage_c3nav_title, conferenceName))
        overrideActionText(context.getString(R.string.snack_engage_c3nav_action))
        withConditions(
                NeverAgainWhenClickedOnce(),
                AfterNumberOfOpportunities(7),
                IsConnectedViaWiFiOrUnknown()
        )
    }

    override fun createSnackBar(snackContext: SnackContext): Snackbar {
        val snackBar = super.createSnackBar(snackContext)
        val textView = snackBar.view.requireViewByIdCompat<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.c3nav, 0, 0, 0)
        val pixelOffset = context.resources.getDimensionPixelOffset(R.dimen.snack_engage_c3nav_pixel_offset)
        textView.compoundDrawablePadding = pixelOffset
        return snackBar
    }

}
