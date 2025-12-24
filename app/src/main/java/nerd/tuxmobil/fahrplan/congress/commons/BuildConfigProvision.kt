package nerd.tuxmobil.fahrplan.congress.commons

interface BuildConfigProvision {
    val packageName: String
    val versionName: String
    val versionCode: Int
    val eventPostalAddress: String
    val eventWebsiteUrl: String
    val showAppDisclaimer: Boolean
    val translationPlatformUrl: String
    val sourceCodeUrl: String
    val issuesUrl: String
    val fDroidUrl: String
    val googlePlayUrl: String
    val dataPrivacyStatementDeUrl: String
    val enableFosdemRoomStates: Boolean
    val serverBackendType: String
}
