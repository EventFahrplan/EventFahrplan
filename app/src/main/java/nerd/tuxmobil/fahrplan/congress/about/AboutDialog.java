package nerd.tuxmobil.fahrplan.congress.about;

import static nerd.tuxmobil.fahrplan.congress.extensions.ViewExtensions.requireViewByIdCompat;

import android.os.Bundle;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.extensions.Strings;
import nerd.tuxmobil.fahrplan.congress.extensions.TextViewExtensions;
import nerd.tuxmobil.fahrplan.congress.utils.LinkMovementMethodCompat;

public class AboutDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = "about";
    private static final String BUNDLE_KEY_SCHEDULE_VERSION =
            BuildConfig.APPLICATION_ID + ".BUNDLE_KEY_SCHEDULE_VERSION";
    private static final String BUNDLE_KEY_SUBTITLE =
            BuildConfig.APPLICATION_ID + ".BUNDLE_KEY_SUBTITLE";
    private static final String BUNDLE_KEY_TITLE =
            BuildConfig.APPLICATION_ID + ".BUNDLE_KEY_TITLE";

    public static AboutDialog newInstance(
            @NonNull String scheduleVersion,
            @NonNull String subtitle,
            @NonNull String title
    ) {
        Bundle arguments = new Bundle();
        arguments.putString(BUNDLE_KEY_SCHEDULE_VERSION, scheduleVersion);
        arguments.putString(BUNDLE_KEY_SUBTITLE, subtitle);
        arguments.putString(BUNDLE_KEY_TITLE, title);
        AboutDialog dialog = new AboutDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    @NonNull
    private String scheduleVersionText = "";

    @NonNull
    private String subtitleText = "";

    @NonNull
    private String titleText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_dialog, container, false);
    }

    @MainThread
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
        Bundle arguments = requireArguments();
        scheduleVersionText = arguments.getString(BUNDLE_KEY_SCHEDULE_VERSION, "");
        subtitleText = arguments.getString(BUNDLE_KEY_SUBTITLE, "");
        titleText = arguments.getString(BUNDLE_KEY_TITLE, "");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView text = requireViewByIdCompat(view, R.id.about_session_version_view);
        if (scheduleVersionText.isEmpty()) {
            text.setVisibility(View.GONE);
        } else {
            text.setVisibility(View.VISIBLE);
            String prefixedScheduleVersionText = getString(R.string.fahrplan) + " " + scheduleVersionText;
            text.setText(prefixedScheduleVersionText);
        }
        text = requireViewByIdCompat(view, R.id.about_session_title_view);
        if (titleText.isEmpty()) {
            titleText = getString(R.string.app_name);
        }
        text.setText(titleText);
        text = requireViewByIdCompat(view, R.id.about_session_subtitle_view);
        if (subtitleText.isEmpty()) {
            subtitleText = getString(R.string.app_hardcoded_subtitle);
        }
        text.setText(subtitleText);
        text = requireViewByIdCompat(view, R.id.about_app_version_view);
        String appVersionText = getString(R.string.appVersion, BuildConfig.VERSION_NAME);
        text.setText(appVersionText);

        View appDisclaimer = requireViewByIdCompat(view, R.id.about_app_disclaimer_view);
        appDisclaimer.setVisibility(BuildConfig.SHOW_APP_DISCLAIMER ? View.VISIBLE : View.GONE);

        int linkTextColor = ContextCompat.getColor(view.getContext(), R.color.text_link_on_dark);
        MovementMethod movementMethod = LinkMovementMethodCompat.getInstance();

        TextView logoCopyright = requireViewByIdCompat(view, R.id.about_copyright_logo_view);
        logoCopyright.setText(Strings.toSpanned(getString(R.string.copyright_logo)));
        logoCopyright.setLinkTextColor(linkTextColor);
        logoCopyright.setMovementMethod(movementMethod);

        TextView conferenceUrl = requireViewByIdCompat(view, R.id.about_conference_url_view);
        String websiteUrl = BuildConfig.EVENT_WEBSITE_URL;
        TextViewExtensions.setLinkText(conferenceUrl, websiteUrl, movementMethod, linkTextColor);

        TextView sourceCode = requireViewByIdCompat(view, R.id.about_source_code_view);
        String sourceCodeUrl = BuildConfig.SOURCE_CODE_URL;
        String sourceCodeTitle = getString(R.string.about_source_code);
        TextViewExtensions.setLinkText(sourceCode, sourceCodeUrl, sourceCodeTitle, movementMethod, linkTextColor);

        TextView issues = requireViewByIdCompat(view, R.id.about_issues_view);
        String issuesUrl = BuildConfig.ISSUES_URL;
        String issuesTitle = getString(R.string.about_issues_or_feature_requests);
        TextViewExtensions.setLinkText(issues, issuesUrl, issuesTitle, movementMethod, linkTextColor);

        TextView fdroidStore = requireViewByIdCompat(view, R.id.about_f_droid_view);
        String fdroidUrl = BuildConfig.F_DROID_URL;
        //noinspection ConstantConditions
        if (fdroidUrl.isEmpty()) {
            fdroidStore.setVisibility(View.GONE);
        } else {
            fdroidStore.setVisibility(View.VISIBLE);
            String fdroidListingTitle = getString(R.string.about_f_droid_listing);
            TextViewExtensions.setLinkText(fdroidStore, fdroidUrl, fdroidListingTitle, movementMethod, linkTextColor);
        }

        TextView googlePlayStore = requireViewByIdCompat(view, R.id.about_google_play_view);
        String googlePlayUrl = BuildConfig.GOOGLE_PLAY_URL;
        String googlePlayListingTitle = getString(R.string.about_google_play_listing);
        TextViewExtensions.setLinkText(googlePlayStore, googlePlayUrl, googlePlayListingTitle, movementMethod, linkTextColor);

        TextView librariesStatement = requireViewByIdCompat(view, R.id.about_libraries_view);
        String libraryNames = getString(R.string.about_libraries_names);
        String librariesStatementText = getString(R.string.about_libraries_statement, libraryNames);
        librariesStatement.setText(librariesStatementText);

        TextView dataPrivacyStatement = requireViewByIdCompat(view, R.id.about_data_privacy_statement_view);
        String dataPrivacyStatementGermanUrl = BuildConfig.DATA_PRIVACY_STATEMENT_DE_URL;
        String dataPrivacyStatementGermanTitle = getString(R.string.about_data_privacy_statement_german);
        TextViewExtensions.setLinkText(dataPrivacyStatement, dataPrivacyStatementGermanUrl, dataPrivacyStatementGermanTitle, movementMethod, linkTextColor);

        // Build information

        TextView buildTimeTextView = requireViewByIdCompat(view, R.id.build_time);
        String buildTimeValue = getString(R.string.build_time);
        String buildTimeText = getString(R.string.build_info_time, buildTimeValue);
        buildTimeTextView.setText(buildTimeText);

        TextView versionCodeTextView = requireViewByIdCompat(view, R.id.build_version_code);
        String versionCodeText = getString(R.string.build_info_version_code, "" + BuildConfig.VERSION_CODE);
        versionCodeTextView.setText(versionCodeText);

        TextView buildHashTextView = requireViewByIdCompat(view, R.id.build_hash);
        String buildHashValue = getString(R.string.git_sha);
        String buildHashText = getString(R.string.build_info_hash, buildHashValue);
        buildHashTextView.setText(buildHashText);
    }
}
