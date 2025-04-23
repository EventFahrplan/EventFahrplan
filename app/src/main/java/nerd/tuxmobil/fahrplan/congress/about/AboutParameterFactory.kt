package nerd.tuxmobil.fahrplan.congress.about

import androidx.annotation.StringRes
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.TextResource
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Empty
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Html
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.PostalAddress
import nerd.tuxmobil.fahrplan.congress.models.Meta

class AboutParameterFactory(
    private val buildConfig: BuildConfigProvision,
    private val resourceResolving: ResourceResolving,
) {

    fun createAboutParameter(meta: Meta): AboutParameter {
        val scheduleVersion = meta.version
        val scheduleVersionText = if (scheduleVersion.isEmpty()) ""
        else "${resourceResolving.getString(R.string.fahrplan)} $scheduleVersion"

        val title = meta.title
        val titleText = title.ifEmpty { resourceResolving.getString(R.string.app_name) }

        val subtitle = meta.subtitle
        val subtitleText = subtitle.ifEmpty {
            resourceResolving.getString(R.string.app_hardcoded_subtitle).ifEmpty { "" }
        }

        return AboutParameter(
            title = titleText,
            subtitle = subtitleText,
            eventLocation = PostalAddress(buildConfig.eventPostalAddress),
            eventUrl = textResourceOf(url = buildConfig.eventWebsiteUrl),
            scheduleVersion = scheduleVersionText,
            appVersion = if (buildConfig.versionName.isEmpty()) "" else resourceResolving.getString(
                R.string.appVersion,
                buildConfig.versionName
            ),

            usageNote = resourceResolving.getString(R.string.usage),
            appDisclaimer = if (buildConfig.showAppDisclaimer) resourceResolving.getString(R.string.app_disclaimer) else "",
            logoCopyright = Html(resourceResolving.getString(R.string.copyright_logo)),

            translationPlatform = textResourceOf(R.string.about_translation_platform, buildConfig.translationPlatformUrl),

            sourceCode = textResourceOf(R.string.about_source_code, buildConfig.sourceCodeUrl),
            issues = textResourceOf(R.string.about_issues_or_feature_requests, buildConfig.issuesUrl),
            fDroid = textResourceOf(R.string.about_f_droid_listing, buildConfig.fDroidUrl),
            googlePlay = textResourceOf(R.string.about_google_play_listing, buildConfig.googlePlayUrl),

            libraries = resourceResolving.getString(
                R.string.about_libraries_statement,
                resourceResolving.getString(R.string.about_libraries_names)
            ),
            dataPrivacyStatement = textResourceOf(R.string.about_data_privacy_statement_german, buildConfig.dataPrivacyStatementDeUrl),
            copyrightNotes = resourceResolving.getString(R.string.copyright_notes),

            buildTime = resourceResolving.getString(
                R.string.build_info_time,
                resourceResolving.getString(R.string.build_time)
            ),
            modifiedAt = resourceResolving.getString(
                R.string.modified_at,
                resourceResolving.getString(R.string.modification_time),
            ),
            buildVersion = resourceResolving.getString(
                R.string.build_info_version_code,
                "${buildConfig.versionCode}"
            ),
            buildHash = resourceResolving.getString(
                R.string.build_info_hash,
                resourceResolving.getString(R.string.git_sha)
            ),
        )
    }

    private fun textResourceOf(@StringRes text: Int? = null, url: String? = null): TextResource {
        return if (url.isNullOrEmpty()) {
            Empty
        } else {
            val title = if (text == null) null else resourceResolving.getString(text)
            Html.of(url = url, text = title)
        }
    }

}
