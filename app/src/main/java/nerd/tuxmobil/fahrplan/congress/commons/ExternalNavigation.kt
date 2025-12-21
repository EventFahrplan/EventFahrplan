package nerd.tuxmobil.fahrplan.congress.commons

interface ExternalNavigation {

    fun openMap(locationText: String)
    fun getBrowsableApps(link: String): List<String>
    fun openLink(link: String)
    fun openLinkWithApp(link: String, packageName: String)

}
