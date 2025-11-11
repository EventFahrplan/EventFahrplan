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
import nerd.tuxmobil.fahrplan.congress.utils.EngelsystemUrlValidator

@Composable
internal fun EngelsystemUrlDialog(
    currentValue: String?,
    onValueChanged: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val validator = remember(context) {
        EngelsystemUrlValidator(
            resourceResolver = ResourceResolver(context),
            urlTypeName = "Engelsystem",
        )
    }

    PreferenceTextInputDialog(
        title = stringResource(R.string.preference_title_engelsystem_json_export_url),
        value = currentValue.orEmpty(),
        placeholder = stringResource(R.string.preference_hint_engelsystem_json_export_url),
        validator = validator,
        onValueChanged = onValueChanged,
        onDismiss = onDismiss,
    )
}

@PreviewLightDark
@Composable
internal fun EngelsystemUrlDialogPreview() {
    EventFahrplanTheme {
        EngelsystemUrlDialog(
            currentValue = "",
            onValueChanged = {},
            onDismiss = {},
        )
    }
}
