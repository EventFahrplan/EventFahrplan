package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.settings.widgets.PreferenceTextInputDialog
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_JSON
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_XML
import nerd.tuxmobil.fahrplan.congress.utils.UrlValidator

@Composable
internal fun ScheduleUrlDialog(
    currentValue: String,
    scheduleFileFormat: ScheduleFileFormat,
    onValueChanged: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val urlTypeName = stringResource(R.string.preference_url_type_friendly_name_alternative_schedule)
    val urlValidator = remember(context, urlTypeName) {
        UrlValidator(
            resourceResolver = ResourceResolver(context),
            urlTypeName = urlTypeName,
        )
    }

    val placeholder = when (scheduleFileFormat) {
        SCHEDULE_V1_JSON -> R.string.preference_hint_alternative_schedule_json_url
        SCHEDULE_V1_XML -> R.string.preference_hint_alternative_schedule_xml_url
    }
    PreferenceTextInputDialog(
        title = stringResource(R.string.preference_title_alternative_schedule_url),
        value = currentValue,
        placeholder = stringResource(placeholder),
        validator = urlValidator,
        onValueChanged = onValueChanged,
        onDismiss = onDismiss,
    )
}

@PreviewLightDark
@Composable
internal fun ScheduleUrlDialogPreview() {
    EventFahrplanTheme {
        ScheduleUrlDialog(
            scheduleFileFormat = SCHEDULE_V1_XML,
            currentValue = "",
            onValueChanged = {},
            onDismiss = {},
        )
    }
}
