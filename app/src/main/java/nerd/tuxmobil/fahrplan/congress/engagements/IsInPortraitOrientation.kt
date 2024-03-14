package nerd.tuxmobil.fahrplan.congress.engagements

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import org.ligi.snackengage.SnackContext
import org.ligi.snackengage.conditions.SnackCondition
import org.ligi.snackengage.snacks.Snack

class IsInPortraitOrientation : SnackCondition {

    override fun isAppropriate(context: SnackContext, snack: Snack) =
        context.androidContext.applicationContext.resources.configuration.orientation == ORIENTATION_PORTRAIT

}
