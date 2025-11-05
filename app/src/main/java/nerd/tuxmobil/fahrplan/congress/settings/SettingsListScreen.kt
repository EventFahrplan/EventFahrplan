package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.TopBar
import nerd.tuxmobil.fahrplan.congress.designsystem.templates.Scaffold
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.settings.widgets.PreferenceCategory
import nerd.tuxmobil.fahrplan.congress.settings.widgets.SwitchPreference

@Composable
internal fun SettingsListScreen(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.settings),
                onBack = onBack,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            CategoryGeneral(state, onViewEvent)
        }
    }
}

@Composable
private fun CategoryGeneral(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(text = stringResource(R.string.general_settings)) {
        SwitchPreference(
            title = stringResource(R.string.preference_title_use_device_time_zone_enabled),
            subtitle = stringResource(R.string.preference_summary_use_device_time_zone_enabled),
            checked = state.settings.isUseDeviceTimeZoneEnabled,
            onCheckedChange = { onViewEvent(SettingsEvent.DeviceTimezoneClicked) },
        )
    }
}

@PreviewLightDark
@Composable
internal fun SettingsListScreenPreview() {
    EventFahrplanTheme {
        SettingsListScreen(
            state = SettingsUiState(),
            onViewEvent = {},
            onBack = {},
        )
    }
}
