package nerd.tuxmobil.fahrplan.congress.about

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Empty
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Html
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.PostalAddress
import nerd.tuxmobil.fahrplan.congress.models.Meta
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class AboutParameterFactoryTest {

    @Test
    fun `createAboutParameter returns AboutParameter with all properties filled`() {
        val factory = AboutParameterFactory(CompleteBuildConfigProvider, CompleteResourceResolver)
        val meta = Meta(
            version = "2024-01-21 21:26",
            title = "37th Chaos Communication Congress",
            subtitle = "Unlocked",
        )
        val expected = AboutParameter(
            title = "37th Chaos Communication Congress",
            subtitle = "Unlocked",
            eventLocation = PostalAddress("Congressplatz 1, 20355 Hamburg"),
            eventUrl = Html.of("https://events.ccc.de/congress/2023/"),
            scheduleVersion = "Fahrplan 2024-01-21 21:26",
            appVersion = "App Version 1.63.2",
            usageNote = "Usage note",
            appDisclaimer = "App disclaimer",
            logoCopyright = Html("""Logo by <a href="https://eventfahrplan.eu">eventfahrplan.eu</a>"""),
            translationPlatform = Html.of(text = "Translation platform", url = "https://crowdin.com/project/eventfahrplan"),
            sourceCode = Html.of(text = "Source code", url = "https://github.com/EventFahrplan/EventFahrplan"),
            issues = Html.of(text = "Issues or feature requests", url = "https://github.com/EventFahrplan/EventFahrplan/issues"),
            fDroid = Html.of(text = "F-Droid listing", url = "https://f-droid.org/packages/info.metadude.android.congress.schedule"),
            googlePlay = Html.of(text = "Google Play listing", url = "https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"),
            libraries = "This application uses the following libraries: Jetpack Compose",
            dataPrivacyStatement = Html.of(text = "Data privacy statement (German)", url = "https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"),
            copyrightNotes = "Copyright",
            buildTime = "Build time: 2015-12-27T13:42Z",
            buildVersion = "Version code: 100",
            buildHash = "Version hash: e1f2g3h-dirty",
        )
        assertThat(factory.createAboutParameter(meta)).isEqualTo(expected)
    }

    @Test
    fun `createAboutParameter returns AboutParameter with some properties filled with hardcoded values`() {
        val factory = AboutParameterFactory(CompleteBuildConfigProvider, CompleteResourceResolver)
        val meta = Meta(
            version = "",
            title = "",
            subtitle = "",
        )
        val expected = AboutParameter(
            title = "37C3 Schedule",
            subtitle = "December 27–30 2023, Congress Center Hamburg",
            eventLocation = PostalAddress("Congressplatz 1, 20355 Hamburg"),
            eventUrl = Html.of("https://events.ccc.de/congress/2023/"),
            scheduleVersion = "",
            appVersion = "App Version 1.63.2",
            usageNote = "Usage note",
            appDisclaimer = "App disclaimer",
            logoCopyright = Html("""Logo by <a href="https://eventfahrplan.eu">eventfahrplan.eu</a>"""),
            translationPlatform = Html.of(text = "Translation platform", url = "https://crowdin.com/project/eventfahrplan"),
            sourceCode = Html.of(text = "Source code", url = "https://github.com/EventFahrplan/EventFahrplan"),
            issues = Html.of(text = "Issues or feature requests", url = "https://github.com/EventFahrplan/EventFahrplan/issues"),
            fDroid = Html.of(text = "F-Droid listing", url = "https://f-droid.org/packages/info.metadude.android.congress.schedule"),
            googlePlay = Html.of(text = "Google Play listing", url = "https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"),
            libraries = "This application uses the following libraries: Jetpack Compose",
            dataPrivacyStatement = Html.of(text = "Data privacy statement (German)", url = "https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"),
            copyrightNotes = "Copyright",
            buildTime = "Build time: 2015-12-27T13:42Z",
            buildVersion = "Version code: 100",
            buildHash = "Version hash: e1f2g3h-dirty",
        )
        assertThat(factory.createAboutParameter(meta)).isEqualTo(expected)
    }

    @Test
    fun `createAboutParameter returns AboutParameter with some empty properties`() {
        val factory = AboutParameterFactory(IncompleteBuildConfigProvider, SomeEmptyResourceResolver)
        val meta = Meta(
            version = "",
            title = "",
            subtitle = "",
        )
        val expected = AboutParameter(
            title = "37C3 Schedule",
            subtitle = "",
            eventLocation = PostalAddress("Congressplatz 1, 20355 Hamburg"),
            eventUrl = Html.of("https://events.ccc.de/congress/2023/"),
            scheduleVersion = "",
            appVersion = "",
            usageNote = "Usage note",
            appDisclaimer = "",
            logoCopyright = Html.of("Logo by eventfahrplan.eu"),
            translationPlatform = Html.of(text = "Translation platform", url = "https://crowdin.com/project/eventfahrplan"),
            sourceCode = Html.of(text = "Source code", url = "https://github.com/EventFahrplan/EventFahrplan"),
            issues = Html.of(text = "Issues or feature requests", url = "https://github.com/EventFahrplan/EventFahrplan/issues"),
            fDroid = Empty,
            googlePlay = Html.of(text = "Google Play listing", url = "https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"),
            libraries = "This application uses the following libraries: Jetpack Compose",
            dataPrivacyStatement = Html.of(text = "Data privacy statement (German)", url = "https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"),
            copyrightNotes = "Copyright",
            buildTime = "Build time: 2015-12-27T13:42Z",
            buildVersion = "Version code: 200",
            buildHash = "Version hash: e1f2g3h-dirty",
        )
        assertThat(factory.createAboutParameter(meta)).isEqualTo(expected)
    }

}

private object CompleteResourceResolver : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.app_name -> "37C3 Schedule"
        R.string.app_hardcoded_subtitle -> "December 27–30 2023, Congress Center Hamburg"
        R.string.fahrplan -> "Fahrplan"
        R.string.appVersion -> "App Version ${formatArgs.first()}"
        R.string.usage -> "Usage note"
        R.string.app_disclaimer -> "App disclaimer"
        R.string.copyright_logo -> """Logo by <a href="https://eventfahrplan.eu">eventfahrplan.eu</a>"""
        R.string.about_translation_platform -> "Translation platform"
        R.string.about_source_code -> "Source code"
        R.string.about_issues_or_feature_requests -> "Issues or feature requests"
        R.string.about_f_droid_listing -> "F-Droid listing"
        R.string.about_google_play_listing -> "Google Play listing"
        R.string.about_libraries_statement -> "This application uses the following libraries: ${formatArgs.first()}"
        R.string.about_libraries_names -> "Jetpack Compose"
        R.string.about_data_privacy_statement_german -> "Data privacy statement (German)"
        R.string.copyright_notes -> "Copyright"
        R.string.build_time -> "2015-12-27T13:42Z"
        R.string.build_info_time -> "Build time: ${formatArgs.first()}"
        R.string.build_info_version_code -> "Version code: ${formatArgs.first()}"
        R.string.git_sha -> "e1f2g3h-dirty"
        R.string.build_info_hash -> "Version hash: ${formatArgs.first()}"
        else -> fail("Unknown string id : $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        throw NotImplementedError("Not needed for this test.")
    }
}

private object SomeEmptyResourceResolver : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.app_name -> "37C3 Schedule"
        R.string.app_hardcoded_subtitle -> ""
        R.string.fahrplan -> "Fahrplan"
        R.string.appVersion -> "App Version ${formatArgs.first()}"
        R.string.usage -> "Usage note"
        R.string.app_disclaimer -> "App disclaimer"
        R.string.copyright_logo -> "Logo by eventfahrplan.eu"
        R.string.about_translation_platform -> "Translation platform"
        R.string.about_source_code -> "Source code"
        R.string.about_issues_or_feature_requests -> "Issues or feature requests"
        R.string.about_f_droid_listing -> "F-Droid listing"
        R.string.about_google_play_listing -> "Google Play listing"
        R.string.about_libraries_statement -> "This application uses the following libraries: ${formatArgs.first()}"
        R.string.about_libraries_names -> "Jetpack Compose"
        R.string.about_data_privacy_statement_german -> "Data privacy statement (German)"
        R.string.copyright_notes -> "Copyright"
        R.string.build_time -> "2015-12-27T13:42Z"
        R.string.build_info_time -> "Build time: ${formatArgs.first()}"
        R.string.build_info_version_code -> "Version code: ${formatArgs.first()}"
        R.string.git_sha -> "e1f2g3h-dirty"
        R.string.build_info_hash -> "Version hash: ${formatArgs.first()}"
        else -> fail("Unknown string id : $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        throw NotImplementedError("Not needed for this test.")
    }
}

private object CompleteBuildConfigProvider : BuildConfigProvision {
    override val versionName: String = "1.63.2"
    override val versionCode: Int = 100
    override val eventPostalAddress: String = "Congressplatz 1, 20355 Hamburg"
    override val eventWebsiteUrl: String = "https://events.ccc.de/congress/2023/"
    override val showAppDisclaimer: Boolean = true
    override val translationPlatformUrl: String = "https://crowdin.com/project/eventfahrplan"
    override val sourceCodeUrl: String = "https://github.com/EventFahrplan/EventFahrplan"
    override val issuesUrl: String = "https://github.com/EventFahrplan/EventFahrplan/issues"
    override val fDroidUrl: String = "https://f-droid.org/packages/info.metadude.android.congress.schedule"
    override val googlePlayUrl: String = "https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"
    override val dataPrivacyStatementDeUrl: String = "https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"
}

private object IncompleteBuildConfigProvider : BuildConfigProvision {
    override val versionName: String = ""
    override val versionCode: Int = 200
    override val eventPostalAddress: String = "Congressplatz 1, 20355 Hamburg"
    override val eventWebsiteUrl: String = "https://events.ccc.de/congress/2023/"
    override val showAppDisclaimer: Boolean = false
    override val translationPlatformUrl: String = "https://crowdin.com/project/eventfahrplan"
    override val sourceCodeUrl: String = "https://github.com/EventFahrplan/EventFahrplan"
    override val issuesUrl: String = "https://github.com/EventFahrplan/EventFahrplan/issues"
    override val fDroidUrl: String = ""
    override val googlePlayUrl: String = "https://play.google.com/store/apps/details?id=info.metadude.android.congress.schedule"
    override val dataPrivacyStatementDeUrl: String = "https://github.com/EventFahrplan/EventFahrplan/blob/master/DATA-PRIVACY-DE.md"
}
