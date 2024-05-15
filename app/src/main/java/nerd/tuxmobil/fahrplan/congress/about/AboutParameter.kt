package nerd.tuxmobil.fahrplan.congress.about

import nerd.tuxmobil.fahrplan.congress.commons.TextResource
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Empty

data class AboutParameter(
    val title: String = "",
    val subtitle: String = "",
    val eventLocation: TextResource = Empty,
    val eventUrl: TextResource = Empty,
    val scheduleVersion: String = "",
    val appVersion: String = "",
    val usageNote: String = "",
    val appDisclaimer: String = "",
    val logoCopyright: TextResource = Empty,
    val translationPlatform: TextResource = Empty,
    val sourceCode: TextResource = Empty,
    val issues: TextResource = Empty,
    val fDroid: TextResource = Empty,
    val googlePlay: TextResource = Empty,
    val libraries: String = "",
    val dataPrivacyStatement: TextResource = Empty,
    val copyrightNotes: String = "",
    val buildTime: String = "",
    val buildVersion: String = "",
    val buildHash: String = "",
)
