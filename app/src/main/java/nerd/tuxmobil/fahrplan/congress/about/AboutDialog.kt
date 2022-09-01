package nerd.tuxmobil.fahrplan.congress.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.setLinkText
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.utils.LinkMovementMethodCompat

class AboutDialog : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "AboutDialog"
        private const val BUNDLE_KEY_SCHEDULE_VERSION = "${BuildConfig.APPLICATION_ID}.BUNDLE_KEY_SCHEDULE_VERSION"
        private const val BUNDLE_KEY_SUBTITLE = "${BuildConfig.APPLICATION_ID}.BUNDLE_KEY_SUBTITLE"
        private const val BUNDLE_KEY_TITLE = "${BuildConfig.APPLICATION_ID}.BUNDLE_KEY_TITLE"

        fun newInstance(scheduleVersion: String, subtitle: String, title: String) =
            AboutDialog().withArguments(
                BUNDLE_KEY_SCHEDULE_VERSION to scheduleVersion,
                BUNDLE_KEY_SUBTITLE to subtitle,
                BUNDLE_KEY_TITLE to title
            )
    }

    private var scheduleVersionText = ""
    private var subtitleText = ""
    private var titleText = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.about_dialog, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(requireArguments()) {
            scheduleVersionText = getString(BUNDLE_KEY_SCHEDULE_VERSION, "")
            subtitleText = getString(BUNDLE_KEY_SUBTITLE, "")
            titleText = getString(BUNDLE_KEY_TITLE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Schedule version
        val scheduleVersion = view.requireViewByIdCompat<TextView>(R.id.about_session_version_view)
        if (scheduleVersionText.isEmpty()) {
            scheduleVersion.isVisible = true
        } else {
            scheduleVersion.isVisible = false
            scheduleVersion.text = "${getString(R.string.fahrplan)} $scheduleVersionText"
        }

        // Session title
        val title = view.requireViewByIdCompat<TextView>(R.id.about_session_title_view)
        if (titleText.isEmpty()) {
            titleText = getString(R.string.app_name)
        }
        title.text = titleText

        // Session subtitle
        val subtitle = view.requireViewByIdCompat<TextView>(R.id.about_session_subtitle_view)
        if (subtitleText.isEmpty()) {
            subtitleText = getString(R.string.app_hardcoded_subtitle)
        }
        subtitle.text = subtitleText

        // App version
        val appVersion = view.requireViewByIdCompat<TextView>(R.id.about_app_version_view)
        appVersion.text = getString(R.string.appVersion, BuildConfig.VERSION_NAME)

        // App disclaimer
        val appDisclaimer = view.requireViewByIdCompat<View>(R.id.about_app_disclaimer_view)
        appDisclaimer.isVisible = BuildConfig.SHOW_APP_DISCLAIMER
        val linkTextColor = ContextCompat.getColor(view.context, R.color.text_link_on_dark)
        val movementMethod = LinkMovementMethodCompat.getInstance()

        // Logo copyright note
        val logoCopyright = view.requireViewByIdCompat<TextView>(R.id.about_copyright_logo_view)
        logoCopyright.text = getString(R.string.copyright_logo).toSpanned()
        logoCopyright.setLinkTextColor(linkTextColor)
        logoCopyright.movementMethod = movementMethod

        // Event website URL
        val conferenceUrl = view.requireViewByIdCompat<TextView>(R.id.about_conference_url_view)
        val websiteUrl = BuildConfig.EVENT_WEBSITE_URL
        conferenceUrl.setLinkText(websiteUrl, null, movementMethod, linkTextColor)

        // Source code link
        val sourceCode = view.requireViewByIdCompat<TextView>(R.id.about_source_code_view)
        val sourceCodeUrl = BuildConfig.SOURCE_CODE_URL
        val sourceCodeTitle = getString(R.string.about_source_code)
        sourceCode.setLinkText(sourceCodeUrl, sourceCodeTitle, movementMethod, linkTextColor)

        // Issues link
        val issues = view.requireViewByIdCompat<TextView>(R.id.about_issues_view)
        val issuesUrl = BuildConfig.ISSUES_URL
        val issuesTitle = getString(R.string.about_issues_or_feature_requests)
        issues.setLinkText(issuesUrl, issuesTitle, movementMethod, linkTextColor)

        // F-Droid store link
        val fdroidStore = view.requireViewByIdCompat<TextView>(R.id.about_f_droid_view)
        val fdroidUrl = BuildConfig.F_DROID_URL
        if (fdroidUrl.isEmpty()) {
            fdroidStore.isVisible = false
        } else {
            fdroidStore.isVisible = true
            val fdroidListingTitle = getString(R.string.about_f_droid_listing)
            fdroidStore.setLinkText(fdroidUrl, fdroidListingTitle, movementMethod, linkTextColor)
        }

        // Google Play store link
        val googlePlayStore = view.requireViewByIdCompat<TextView>(R.id.about_google_play_view)
        val googlePlayUrl = BuildConfig.GOOGLE_PLAY_URL
        val googlePlayListingTitle = getString(R.string.about_google_play_listing)
        googlePlayStore.setLinkText(
            googlePlayUrl,
            googlePlayListingTitle,
            movementMethod,
            linkTextColor
        )

        // Libraries statement
        val librariesStatement = view.requireViewByIdCompat<TextView>(R.id.about_libraries_view)
        val libraryNames = getString(R.string.about_libraries_names)
        val librariesStatementText = getString(R.string.about_libraries_statement, libraryNames)
        librariesStatement.text = librariesStatementText

        // Privacy statement
        val dataPrivacyStatement = view.requireViewByIdCompat<TextView>(R.id.about_data_privacy_statement_view)
        val dataPrivacyStatementGermanUrl = BuildConfig.DATA_PRIVACY_STATEMENT_DE_URL
        val dataPrivacyStatementGermanTitle =
            getString(R.string.about_data_privacy_statement_german)
        dataPrivacyStatement.setLinkText(
            dataPrivacyStatementGermanUrl,
            dataPrivacyStatementGermanTitle,
            movementMethod,
            linkTextColor
        )

        // Build time
        val buildTimeTextView = view.requireViewByIdCompat<TextView>(R.id.build_time)
        val buildTimeValue = getString(R.string.build_time)
        val buildTimeText = getString(R.string.build_info_time, buildTimeValue)
        buildTimeTextView.text = buildTimeText

        // Build version
        val versionCodeTextView = view.requireViewByIdCompat<TextView>(R.id.build_version_code)
        val versionCodeText =
            getString(R.string.build_info_version_code, "" + BuildConfig.VERSION_CODE)
        versionCodeTextView.text = versionCodeText

        // Build hash
        val buildHashTextView = view.requireViewByIdCompat<TextView>(R.id.build_hash)
        val buildHashValue = getString(R.string.git_sha)
        val buildHashText = getString(R.string.build_info_hash, buildHashValue)
        buildHashTextView.text = buildHashText
    }

}
