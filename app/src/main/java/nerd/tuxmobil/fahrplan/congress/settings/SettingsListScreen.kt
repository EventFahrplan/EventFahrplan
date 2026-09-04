package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.alarmTimeToUiString
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.NavigationBarProtection
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
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.InsistentAlarmClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleRefreshIntervalClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleStatisticClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ShowScheduleUpdateDialogClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ShowOnLockscreenClicked
import nerd.tuxmobil.fahrplan.congress.settings.widgets.AlternativeScheduleUrlPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.ClickPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.EnableAutomaticUpdatesPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.EngelsystemShiftsUrlPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.ExternalClickPreference
import nerd.tuxmobil.fahrplan.congress.settings.widgets.PreferenceCategory
import nerd.tuxmobil.fahrplan.congress.settings.widgets.SocialMediaHashtagsHandlesPreference
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
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
            ) {
                val showDivider = LocalWindowInfo.current.containerDpSize.width > 1000.dp
                if (state.isDevelopmentCategoryVisible) {
                    CategoryDevelopment(state, showDivider, onViewEvent)
                }

                CategoryGeneral(state, showDivider, onViewEvent)
                CategoryAlarms(state, showDivider, onViewEvent)
                CategorySharing(state, onViewEvent)

                if (state.isEngelsystemCategoryVisible) {
                    CategoryEngelsystem(state, onViewEvent)
                }
            }
            NavigationBarProtection(Modifier.align(BottomCenter))
        }
    }
}

@Composable
private fun CategoryDevelopment(
    state: SettingsUiState,
    showDivider: Boolean,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.development_settings)) {
        ClickPreference(
            title = stringResource(R.string.preference_title_schedule_refresh_interval),
            subtitle = state.settings.scheduleRefreshIntervalToUiString(),
            showDivider = showDivider,
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
    showDivider: Boolean,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.general_settings)) {
        EnableAutomaticUpdatesPreference(
            isAutoUpdateEnabled = state.settings.isAutoUpdateEnabled,
            nextFetch = state.nextFetch,
            showDivider = showDivider,
            onViewEvent = onViewEvent,
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_show_schedule_update_dialog_enabled),
            subtitle = stringResource(R.string.preference_summary_show_schedule_update_dialog_enabled),
            checked = state.settings.isShowScheduleUpdateDialogEnabled,
            showDivider = showDivider,
            onCheckedChange = { onViewEvent(ShowScheduleUpdateDialogClicked) },
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_use_device_time_zone_enabled),
            subtitle = stringResource(R.string.preference_summary_use_device_time_zone_enabled),
            checked = state.settings.isUseDeviceTimeZoneEnabled,
            showDivider = showDivider,
            onCheckedChange = { onViewEvent(DeviceTimezoneClicked) },
        )

        if (state.isNotificationSettingsVisible) {
            ExternalClickPreference(
                title = stringResource(R.string.preference_title_app_notification_settings),
                subtitle = stringResource(R.string.preference_summary_app_notification_settings),
                showDivider = showDivider,
                onClick = { onViewEvent(CustomizeNotificationsClicked) },
            )
        }

        if (state.isAlternativeScheduleUrlVisible) {
            AlternativeScheduleUrlPreference(
                alternativeScheduleUrl = state.settings.alternativeScheduleUrl,
                showDivider = showDivider,
                onViewEvent = onViewEvent,
            )
        }

        SwitchPreference(
            title = stringResource(R.string.preference_title_alternative_highlighting_enabled),
            subtitle = stringResource(R.string.preference_summary_alternative_highlighting_enabled),
            checked = state.settings.isAlternativeHighlightingEnabled,
            showDivider = showDivider,
            onCheckedChange = { onViewEvent(AlternativeHighlightingClicked) },
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_fast_swiping_enabled),
            subtitle = stringResource(R.string.preference_summary_fast_swiping_enabled),
            checked = state.settings.isFastSwipingEnabled,
            showDivider = showDivider,
            onCheckedChange = { onViewEvent(FastSwipingClicked) },
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_show_on_lockscreen_enabled),
            subtitle = stringResource(R.string.preference_summary_show_on_lockscreen_enabled),
            checked = state.settings.isShowOnLockscreenEnabled,
            onCheckedChange = { onViewEvent(ShowOnLockscreenClicked) },
        )
    }
}

@Composable
private fun CategoryAlarms(
    state: SettingsUiState,
    showDivider: Boolean,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.reminders)) {
        ExternalClickPreference(
            title = stringResource(R.string.preference_title_alarm_tone),
            subtitle = stringResource(R.string.preference_summary_alarm_tone),
            showDivider = showDivider,
            onClick = { onViewEvent(AlarmToneClicked) },
        )

        SwitchPreference(
            title = stringResource(R.string.preference_title_insistent_alarms_enabled),
            subtitle = stringResource(R.string.preference_summary_insistent_alarms_enabled),
            checked = state.settings.isInsistentAlarmsEnabled,
            showDivider = showDivider,
            onCheckedChange = { onViewEvent(InsistentAlarmClicked) },
        )

        ClickPreference(
            title = stringResource(R.string.preference_title_alarm_time),
            subtitle = state.settings.alarmTimeToUiString(),
            onClick = { onViewEvent(AlarmTimeClicked) },
        )
    }
}

@Composable
private fun CategorySharing(
    state: SettingsUiState,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    PreferenceCategory(stringResource(R.string.preference_sharing_category_title)) {
        SocialMediaHashtagsHandlesPreference(
            socialMediaHashtagsHandles = state.settings.socialMediaHashtagsHandles,
            onViewEvent = onViewEvent,
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
