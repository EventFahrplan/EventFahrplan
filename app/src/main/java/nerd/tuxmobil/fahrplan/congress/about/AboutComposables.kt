package nerd.tuxmobil.fahrplan.congress.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.about.AboutViewEvent.OnPostalAddressClick
import nerd.tuxmobil.fahrplan.congress.commons.ClickableText
import nerd.tuxmobil.fahrplan.congress.commons.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.commons.MultiDevicePreview
import nerd.tuxmobil.fahrplan.congress.commons.TextResource
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Empty
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Html
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.PostalAddress
import nerd.tuxmobil.fahrplan.congress.extensions.toTextUnit

@Composable
internal fun AboutScreen(
    parameter: AboutParameter,
    onViewEvent: (AboutViewEvent) -> Unit,
) {
    EventFahrplanTheme {
        Scaffold { contentPadding ->
            Box(
                Modifier
                    .background(colorResource(R.color.about_window_background))
                    .padding(contentPadding)
                    .fillMaxSize() // Prevent background flickering on load
                    .verticalScroll(rememberScrollState())
            ) {
                Column(Modifier.padding(
                    start = dimensionResource(R.dimen.about_padding_horizontal),
                    top = dimensionResource(R.dimen.about_padding_top),
                    end = dimensionResource(R.dimen.about_padding_horizontal),
                    bottom = dimensionResource(R.dimen.about_padding_bottom)
                )) {
                    EventInfo(parameter, onViewEvent)
                    UsageNote(parameter)
                    AppDisclaimer(parameter)
                    LogoCopyright(parameter)
                    ProjectLinks(parameter)
                    Libraries(parameter)
                    DataPrivacyStatement(parameter)
                    CopyrightNotes(parameter)
                    BuildInfo(parameter)
                }
            }
        }
    }
}

@Composable
private fun EventInfo(parameter: AboutParameter, onViewEvent: (AboutViewEvent) -> Unit) {
    Column(
        Modifier.fillMaxWidth(), // Prevent horizontal flickering on load
        horizontalAlignment = CenterHorizontally
    ) {
        val horizontalTextAlign = TextAlign.Center
        Image(
            modifier = Modifier
                .padding(vertical = 16.dp),
            painter = painterResource(R.drawable.dialog_logo),
            contentDescription = stringResource(R.string.about_logo_content_description)
        )
        if (parameter.title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                color = colorResource(R.color.about_title),
                fontSize = dimensionResource(R.dimen.about_title).toTextUnit(),
                fontWeight = FontWeight.Bold,
                text = parameter.title,
                textAlign = horizontalTextAlign,
            )
        }
        if (parameter.subtitle.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = parameter.subtitle,
                fontSize = dimensionResource(R.dimen.about_subtitle).toTextUnit(),
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = colorResource(R.color.about_subtitle),
                textAlign = horizontalTextAlign,
            )
        }
        AboutClickableText(
            textResource = parameter.eventLocation,
            textAlign = horizontalTextAlign,
            onClick = { onViewEvent(OnPostalAddressClick(it)) },
        )
        AboutClickableText(
            textResource = parameter.eventUrl,
            textAlign = horizontalTextAlign,
        )
        AboutText(
            text = parameter.scheduleVersion,
            textAlign = horizontalTextAlign,
        )
        AboutText(
            text = parameter.appVersion,
            textAlign = horizontalTextAlign,
        )
        if (parameter.eventLocation != Empty ||
            parameter.eventUrl != Empty ||
            parameter.scheduleVersion.isNotEmpty() ||
            parameter.appVersion.isNotEmpty()
        ) {
            SectionDivider()
        }
    }
}

@Composable
private fun UsageNote(parameter: AboutParameter) {
    if (parameter.usageNote.isNotEmpty()) {
        AboutText(text = parameter.usageNote)
        SectionDivider()
    }
}

@Composable
private fun AppDisclaimer(parameter: AboutParameter) {
    if (parameter.appDisclaimer.isNotEmpty()) {
        AboutText(text = parameter.appDisclaimer)
        SectionDivider()
    }
}

@Composable
private fun LogoCopyright(parameter: AboutParameter) {
    if (parameter.logoCopyright != Empty) {
        AboutClickableText(textResource = parameter.logoCopyright)
        SectionDivider()
    }
}

@Composable
private fun ProjectLinks(parameter: AboutParameter) {
    AboutClickableText(
        textResource = parameter.translationPlatform,
    )
    AboutClickableText(
        textResource = parameter.sourceCode,
    )
    AboutClickableText(
        textResource = parameter.issues,
    )
    AboutClickableText(
        textResource = parameter.fDroid,
    )
    AboutClickableText(
        textResource = parameter.googlePlay,
    )
    if (parameter.translationPlatform != Empty ||
        parameter.sourceCode != Empty ||
        parameter.issues != Empty ||
        parameter.fDroid != Empty ||
        parameter.googlePlay != Empty
    ) {
        SectionDivider()
    }
}

@Composable
private fun Libraries(parameter: AboutParameter) {
    if (parameter.libraries.isNotEmpty()) {
        AboutText(text = parameter.libraries)
        SectionDivider()
    }
}

@Composable
private fun DataPrivacyStatement(parameter: AboutParameter) {
    if (parameter.dataPrivacyStatement != Empty) {
        AboutClickableText(
            textResource = parameter.dataPrivacyStatement,
        )
        SectionDivider()
    }
}

@Composable
private fun CopyrightNotes(parameter: AboutParameter) {
    if (parameter.copyrightNotes.isNotEmpty()) {
        AboutText(text = parameter.copyrightNotes)
        SectionDivider()
    }
}

@Composable
private fun BuildInfo(parameter: AboutParameter) {
    AboutText(text = parameter.buildTime)
    AboutText(text = parameter.buildVersion)
    AboutText(text = parameter.buildHash)
}

@Composable
private fun AboutClickableText(
    textResource: TextResource,
    textAlign: TextAlign = TextAlign.Start,
    onClick: (String) -> Unit = {},
) {
    ClickableText(
        textResource = textResource,
        fontSize = dimensionResource(R.dimen.about_text).toTextUnit(), // To match font size of AboutText
        textAlign = textAlign,
        textColor = R.color.about_text,
        textLinkColor = R.color.about_text_link,
        onClick = onClick,
    )
}

@Composable
private fun AboutText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    if (text.isNotEmpty()) {
        Text(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 4.dp),
            text = text,
            fontSize = dimensionResource(R.dimen.about_text).toTextUnit(),
            textAlign = textAlign,
            color = colorResource(R.color.about_text),
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = colorResource(R.color.about_horizontal_line),
        thickness = dimensionResource(R.dimen.about_horizontal_line_height)
    )
}

@MultiDevicePreview
@Composable
private fun AboutScreenPreview() {
    AboutScreen(
        AboutParameter(
            title = "37th Chaos Communication Congress",
            subtitle = "Unlocked",
            eventLocation = PostalAddress("CCH, Congressplatz 1, 20355 Hamburg"),
            eventUrl = Html.of("https://events.ccc.de/congress/2023/"),
            scheduleVersion = "Fahrplan BAD NETWORK/FIREWALL",
            appVersion = "App Version 1.63.2 Kaus Australis; lounges 909; lightning Manwë; thms Tales of Monkey Island; wiki 2023-12-28 12:11",
            usageNote = stringResource(R.string.usage),
            appDisclaimer = stringResource(R.string.app_disclaimer),
            logoCopyright = Html.of(stringResource(R.string.copyright_logo)),
            translationPlatform = Html.of(BuildConfig.TRANSLATION_PLATFORM_URL, stringResource(R.string.about_translation_platform)),
            sourceCode = Html.of(BuildConfig.SOURCE_CODE_URL, stringResource(R.string.about_source_code)),
            issues = Html.of(BuildConfig.ISSUES_URL, stringResource(R.string.about_issues_or_feature_requests)),
            fDroid = Html.of(BuildConfig.F_DROID_URL, stringResource(R.string.about_f_droid_listing)),
            googlePlay = Html.of(BuildConfig.GOOGLE_PLAY_URL, stringResource(R.string.about_google_play_listing)),
            libraries = stringResource(R.string.about_libraries_statement),
            dataPrivacyStatement = Html.of(BuildConfig.DATA_PRIVACY_STATEMENT_DE_URL, stringResource(R.string.about_data_privacy_statement_german)),
            copyrightNotes = stringResource(R.string.copyright_notes),
            buildTime = stringResource(R.string.build_info_time),
            buildVersion = stringResource(R.string.build_info_version_code),
            buildHash = stringResource(R.string.build_info_hash),
        ),
        onViewEvent = {},
    )
}
