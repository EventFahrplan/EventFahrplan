package nerd.tuxmobil.fahrplan.congress.commons

interface ExternalNavigation {

    fun openMap(locationText: String)
    fun getBrowserApps(): List<String>
    fun getDefaultBrowsableApp(): String?
    fun openLink(link: String)
    fun openLinkWithApp(link: String, packageName: String)

}
