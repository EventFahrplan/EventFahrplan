package nerd.tuxmobil.fahrplan.congress;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_dialog, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView text = (TextView) view.findViewById(R.id.eventVersion);
        text.setText(getString(R.string.fahrplan) + " " + MyApp.version);
        text = (TextView) view.findViewById(R.id.eventTitle);
        text.setText(MyApp.title);
        text = (TextView) view.findViewById(R.id.eventSubtitle);
        text.setText(MyApp.subtitle);
        text = (TextView) view.findViewById(R.id.appVersion);
        String appVersionText = getString(R.string.appVersion, BuildConfig.VERSION_NAME);
        text.setText(appVersionText);

        TextView logo_copyright = (TextView) view.findViewById(R.id.copyright_logo);
        logo_copyright.setText(Html.fromHtml(getString(R.string.copyright_logo)));
        logo_copyright.setLinkTextColor(getResources().getColor(R.color.text_link_color_dark));
        logo_copyright.setMovementMethod(LinkMovementMethod.getInstance());

        TextView conferenceUrl = (TextView) view.findViewById(R.id.conference_url);
        conferenceUrl.setText(Html.fromHtml(getString(R.string.conference_url)));
        conferenceUrl.setMovementMethod(LinkMovementMethod.getInstance());
        conferenceUrl.setLinkTextColor(getResources().getColor(R.color.text_link_color_dark));

        TextView sourceCode = (TextView) view.findViewById(R.id.source_code);
        sourceCode.setText(Html.fromHtml(getString(R.string.source_code)));
        sourceCode.setMovementMethod(LinkMovementMethod.getInstance());
        sourceCode.setLinkTextColor(getResources().getColor(R.color.text_link_color_dark));

        TextView issues = (TextView) view.findViewById(R.id.issues);
        issues.setText(Html.fromHtml(getString(R.string.issues)));
        issues.setMovementMethod(LinkMovementMethod.getInstance());
        issues.setLinkTextColor(getResources().getColor(R.color.text_link_color_dark));

        TextView googlePlayStore = (TextView) view.findViewById(R.id.google_play_store);
        googlePlayStore.setText(Html.fromHtml(getString(R.string.google_play_store)));
        googlePlayStore.setMovementMethod(LinkMovementMethod.getInstance());
        googlePlayStore.setLinkTextColor(getResources().getColor(R.color.text_link_color_dark));

        // Build information

        TextView buildTimeTextView = (TextView) view.findViewById(R.id.build_time);
        String buildTimeText = getString(R.string.build_info_time, BuildConfig.BUILD_TIME);
        buildTimeTextView.setText(buildTimeText);

        TextView versionCodeTextView = (TextView) view.findViewById(R.id.build_version_code);
        String versionCodeText = getString(R.string.build_info_version_code, "" + BuildConfig.VERSION_CODE);
        versionCodeTextView.setText(versionCodeText);

        TextView buildHashTextView = (TextView) view.findViewById(R.id.build_hash);
        String buildHashText = getString(R.string.build_info_hash, BuildConfig.GIT_SHA);
        buildHashTextView.setText(buildHashText);
    }
}
