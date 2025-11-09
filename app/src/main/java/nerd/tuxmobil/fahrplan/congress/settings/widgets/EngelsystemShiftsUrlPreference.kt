package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun EngelsystemShiftsUrlPreference(
    engelsystemShiftsUrl: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(
                horizontal = PREFERENCE_HORIZONTAL_PADDING_DP.dp,
                vertical = PREFERENCE_VERTICAL_PADDING_DP.dp,
            )
    ) {
        Text(
            text = stringResource(R.string.preference_title_engelsystem_json_export_url),
            style = EventFahrplanTheme.typography.titleLarge,
        )

        Text(
            text = if (engelsystemShiftsUrl.isNullOrEmpty()) {
                AnnotatedString.fromHtml(
                    stringResource(
                        R.string.preference_summary_engelsystem_json_export_url,
                        stringResource(R.string.engelsystem_alias)
                    )
                )
            } else {
                // Truncate to keep the key private.
                AnnotatedString("${engelsystemShiftsUrl.dropLast(23)}…")
            },
            style = EventFahrplanTheme.typography.bodyMedium,
        )
    }
}
