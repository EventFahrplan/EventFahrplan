package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.preferences.Settings
import nerd.tuxmobil.fahrplan.congress.settings.widgets.PreferenceListDialog

@Composable
internal fun ScheduleRefreshIntervalDialog(
    currentValue: Int,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val entries = persistentMapOf(
        -1 to stringResource(R.string.schedule_refresh_interval_title_unmodified),
        30_000 to stringResource(R.string.schedule_refresh_interval_title_every_30_seconds),
        60_000 to stringResource(R.string.schedule_refresh_interval_title_every_60_seconds),
        120_000 to stringResource(R.string.schedule_refresh_interval_title_every_120_seconds),
    ).toImmutableMap()

    PreferenceListDialog(
        title = stringResource(R.string.preference_dialog_title_schedule_refresh_interval),
        entries = entries,
        selectedOption = currentValue,
        onOptionSelected = onOptionSelected,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun Settings.scheduleRefreshIntervalToUiString(): String {
    return when (scheduleRefreshInterval) {
        -1 -> stringResource(R.string.schedule_refresh_interval_title_unmodified)
        30_000 -> stringResource(R.string.schedule_refresh_interval_title_every_30_seconds)
        60_000 -> stringResource(R.string.schedule_refresh_interval_title_every_60_seconds)
        120_000 -> stringResource(R.string.schedule_refresh_interval_title_every_120_seconds)
        else -> error("Unsupported value: $scheduleRefreshInterval")
    }
}

@PreviewLightDark
@Composable
internal fun ScheduleRefreshIntervalDialogPreview() {
    EventFahrplanTheme {
        ScheduleRefreshIntervalDialog(
            currentValue = -1,
            onOptionSelected = {},
            onDismiss = {},
        )
    }
}
