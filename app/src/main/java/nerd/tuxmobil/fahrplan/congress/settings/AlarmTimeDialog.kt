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
internal fun AlarmTimeDialog(
    currentValue: Int,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val entries = persistentMapOf(
        0 to stringResource(R.string.alarm_time_title_at_start_time),
        5 to stringResource(R.string.alarm_time_title_5_minutes_before),
        10 to stringResource(R.string.alarm_time_title_10_minutes_before),
        15 to stringResource(R.string.alarm_time_title_15_minutes_before),
        20 to stringResource(R.string.alarm_time_title_20_minutes_before),
        30 to stringResource(R.string.alarm_time_title_30_minutes_before),
        45 to stringResource(R.string.alarm_time_title_45_minutes_before),
        60 to stringResource(R.string.alarm_time_title_60_minutes_before),
    ).toImmutableMap()

    PreferenceListDialog(
        title = stringResource(R.string.preference_dialog_title_alarm_time),
        entries = entries,
        selectedOption = currentValue,
        onOptionSelected = onOptionSelected,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun Settings.alarmTimeToUiString(): String? {
    return when (alarmTime) {
        0 -> stringResource(R.string.alarm_time_title_at_start_time)
        5 -> stringResource(R.string.alarm_time_title_5_minutes_before)
        10 -> stringResource(R.string.alarm_time_title_10_minutes_before)
        15 -> stringResource(R.string.alarm_time_title_15_minutes_before)
        20 -> stringResource(R.string.alarm_time_title_20_minutes_before)
        30 -> stringResource(R.string.alarm_time_title_30_minutes_before)
        45 -> stringResource(R.string.alarm_time_title_45_minutes_before)
        60 -> stringResource(R.string.alarm_time_title_60_minutes_before)
        else -> error("Unsupported value: $alarmTime")
    }
}

@PreviewLightDark
@Composable
internal fun AlarmTimeDialogPreview() {
    EventFahrplanTheme {
        AlarmTimeDialog(
            currentValue = 0,
            onOptionSelected = {},
            onDismiss = {},
        )
    }
}

