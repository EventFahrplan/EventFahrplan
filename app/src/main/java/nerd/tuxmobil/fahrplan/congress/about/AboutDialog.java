package nerd.tuxmobil.fahrplan.congress.about;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;

public class AboutDialog extends DialogFragment {

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_dialog, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
        Bundle arguments = getArguments();
        if (arguments != null) {
            scheduleVersionText = arguments.getString(BUNDLE_KEY_SCHEDULE_VERSION, "");
            subtitleText = arguments.getString(BUNDLE_KEY_SUBTITLE, "");
            titleText = arguments.getString(BUNDLE_KEY_TITLE, "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView text = view.findViewById(R.id.about_session_version_view);
        if (scheduleVersionText.isEmpty()) {
            text.setVisibility(View.GONE);
        } else {
            text.setVisibility(View.VISIBLE);
            String prefixedScheduleVersionText = getString(R.string.fahrplan) + " " + scheduleVersionText;
            text.setText(prefixedScheduleVersionText);
        }
        text = view.findViewById(R.id.about_session_title_view);
        if (titleText.isEmpty()) {
            titleText = getString(R.string.app_name);
        }
        text.setText(titleText);
        text = view.findViewById(R.id.about_session_subtitle_view);
        if (subtitleText.isEmpty()) {
            subtitleText = getString(R.string.app_hardcoded_subtitle);
        }
        text.setText(subtitleText);
        text = view.findViewById(R.id.about_app_version_view);
        String appVersionText = getString(R.string.appVersion, BuildConfig.VERSION_NAME);
        text.setText(appVersionText);

        View appDisclaimer = view.findViewById(R.id.about_app_disclaimer_view);
        //noinspection ConstantConditions
        appDisclaimer.setVisibility(BuildConfig.SHOW_APP_DISCLAIMER ? View.VISIBLE : View.GONE);

        int linkTextColor = ContextCompat.getColor(view.getContext(), R.color.text_link_color_dark);
        MovementMethod movementMethod = LinkMovementMethod.getInstance();

        TextView logoCopyright = view.findViewById(R.id.about_copyright_logo_view);
        logoCopyright.setText(Html.fromHtml(getString(R.string.copyright_logo)));
        logoCopyright.setLinkTextColor(linkTextColor);
        logoCopyright.setMovementMethod(movementMethod);

        TextView conferenceUrl = view.findViewById(R.id.about_conference_url_view);
        conferenceUrl.setText(Html.fromHtml(getString(R.string.conference_url)));
        conferenceUrl.setMovementMethod(movementMethod);
        conferenceUrl.setLinkTextColor(linkTextColor);

        TextView sourceCode = view.findViewById(R.id.about_source_code_view);
        sourceCode.setText(Html.fromHtml(getString(R.string.source_code)));
        sourceCode.setMovementMethod(movementMethod);
        sourceCode.setLinkTextColor(linkTextColor);

        TextView issues = view.findViewById(R.id.about_issues_view);
        issues.setText(Html.fromHtml(getString(R.string.issues)));
        issues.setMovementMethod(movementMethod);
        issues.setLinkTextColor(linkTextColor);

        TextView googlePlayStore = view.findViewById(R.id.about_google_play_view);
        googlePlayStore.setText(Html.fromHtml(getString(R.string.google_play_store)));
        googlePlayStore.setMovementMethod(movementMethod);
        googlePlayStore.setLinkTextColor(linkTextColor);

        TextView dataPrivacyStatement = view.findViewById(R.id.about_data_privacy_statement_view);
        dataPrivacyStatement.setText(Html.fromHtml(getString(R.string.about_data_privacy_statement)));
        dataPrivacyStatement.setMovementMethod(movementMethod);
        dataPrivacyStatement.setLinkTextColor(linkTextColor);

        // Build information

        TextView buildTimeTextView = view.findViewById(R.id.build_time);
        String buildTimeText = getString(R.string.build_info_time, BuildConfig.BUILD_TIME);
        buildTimeTextView.setText(buildTimeText);

        TextView versionCodeTextView = view.findViewById(R.id.build_version_code);
        String versionCodeText = getString(R.string.build_info_version_code, "" + BuildConfig.VERSION_CODE);
        versionCodeTextView.setText(versionCodeText);

        TextView buildHashTextView = view.findViewById(R.id.build_hash);
        String buildHashText = getString(R.string.build_info_hash, BuildConfig.GIT_SHA);
        buildHashTextView.setText(buildHashText);
    }
}
