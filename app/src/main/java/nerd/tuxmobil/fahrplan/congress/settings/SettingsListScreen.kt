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
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlarmTimeClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlarmToneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlternativeHighlightingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.CustomizeNotificationsClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.DeviceTimezoneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.EngelsystemUrlClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.FastSwipingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleRefreshIntervalClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleStatisticClicked
import nerd.tuxmobil.fahrplan.congress.settings.widgets.AlternativeScheduleUrlPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.ClickPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.EnableAutomaticUpdatesPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.EngelsystemShiftsUrlPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.ExternalClickPreference
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
            if (state.isDevelopmentCategoryVisible) {
                CategoryDevelopment(state, onViewEvent)
            }

            CategoryGeneral(state, onViewEvent)
            CategoryAlarms(state, onViewEvent)

            if (state.isEngelsystemCategoryVisible) {
                CategoryEngelsystem(state, onViewEvent)
            }
        }
    }
}

@Composable
private fun CategoryDevelopment(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.development_settings)) {
        ClickPreference(
            title = stringResource(R.string.preference_title_schedule_refresh_interval),
            subtitle = state.settings.scheduleRefreshIntervalToUiString(),
            onClick = { onViewEvent(ScheduleRefreshIntervalClicked) },
        )

        ClickPreference(
            title = stringResource(R.string.preference_title_schedule_statistic),
            subtitle = stringResource(R.string.preference_summary_schedule_statistic),
            onClick = { onViewEvent(ScheduleStatisticClicked) },
        )
    }
}

@Composable
private fun CategoryGeneral(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.general_settings)) {
        EnableAutomaticUpdatesPreference(
            isAutoUpdateEnabled = state.settings.isAutoUpdateEnabled,
            nextFetch = state.nextFetch,
            onViewEvent = onViewEvent,
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_use_device_time_zone_enabled),
            subtitle = stringResource(R.string.preference_summary_use_device_time_zone_enabled),
            checked = state.settings.isUseDeviceTimeZoneEnabled,
            onCheckedChange = { onViewEvent(DeviceTimezoneClicked) },
        )

        if (state.isNotificationSettingsVisible) {
            ExternalClickPreference(
                title = stringResource(R.string.preference_title_app_notification_settings),
                subtitle = stringResource(R.string.preference_summary_app_notification_settings),
                onClick = { onViewEvent(CustomizeNotificationsClicked) },
            )
        }

        if (state.isAlternativeScheduleUrlVisible) {
            AlternativeScheduleUrlPreference(
                alternativeScheduleUrl = state.settings.alternativeScheduleUrl,
                onViewEvent = onViewEvent,
            )
        }

        SwitchPreference(
            title = stringResource(R.string.preference_title_alternative_highlighting_enabled),
            subtitle = stringResource(R.string.preference_summary_alternative_highlighting_enabled),
            checked = state.settings.isAlternativeHighlightingEnabled,
            onCheckedChange = { onViewEvent(AlternativeHighlightingClicked) },
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_fast_swiping_enabled),
            subtitle = stringResource(R.string.preference_summary_fast_swiping_enabled),
            checked = state.settings.isFastSwipingEnabled,
            onCheckedChange = { onViewEvent(FastSwipingClicked) },
        )
    }
}

@Composable
private fun CategoryAlarms(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.reminders)) {
        ExternalClickPreference(
            title = stringResource(R.string.preference_title_alarm_tone),
            subtitle = stringResource(R.string.preference_summary_alarm_tone),
            onClick = { onViewEvent(AlarmToneClicked) },
        )

        ClickPreference(
            title = stringResource(R.string.preference_dialog_title_alarm_time),
            subtitle = state.settings.alarmTimeToUiString(),
            onClick = { onViewEvent(AlarmTimeClicked) },
        )
    }
}

@Composable
private fun CategoryEngelsystem(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.preference_engelsystem_category_title)) {
        EngelsystemShiftsUrlPreference(
            engelsystemShiftsUrl = state.settings.engelsystemShiftsUrl,
            onClick = { onViewEvent(EngelsystemUrlClicked) },
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
