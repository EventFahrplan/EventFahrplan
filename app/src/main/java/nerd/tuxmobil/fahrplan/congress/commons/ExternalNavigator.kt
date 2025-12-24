package nerd.tuxmobil.fahrplan.congress.commons

import android.content.Context
import nerd.tuxmobil.fahrplan.congress.extensions.getBrowsableApps
import nerd.tuxmobil.fahrplan.congress.extensions.openLink
import nerd.tuxmobil.fahrplan.congress.extensions.openLinkWithApp
import nerd.tuxmobil.fahrplan.congress.extensions.openMap

class ExternalNavigator(val context: Context) : ExternalNavigation {

    override fun openMap(locationText: String) =
        context.openMap(locationText)

    override fun getBrowsableApps(link: String) =
        context.getBrowsableApps(link)

    override fun openLink(link: String) =
        context.openLink(link)

    override fun openLinkWithApp(link: String, packageName: String) =
        context.openLinkWithApp(link = link, packageName = packageName)

}
