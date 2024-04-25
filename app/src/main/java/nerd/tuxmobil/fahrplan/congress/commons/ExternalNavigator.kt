package nerd.tuxmobil.fahrplan.congress.commons

import android.content.Context
import nerd.tuxmobil.fahrplan.congress.extensions.openMap

class ExternalNavigator(val context: Context) : ExternalNavigation {

    override fun openMap(locationText: String) = context.openMap(locationText)

}
