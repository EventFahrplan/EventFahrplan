package nerd.tuxmobil.fahrplan.congress.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.ALTERNATIVE_HIGHLIGHTING_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.ENGELSYSTEM_SHIFTS_URL_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.SCHEDULE_URL_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.USE_DEVICE_TIME_ZONE_UPDATED
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.LaunchNotificationSettingsScreen
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.PickAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlarmTimeClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlarmToneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlternativeHighlightingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlternativeScheduleUrlClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AutoUpdateClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.CustomizeNotificationsClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.DeviceTimezoneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.EngelsystemUrlClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.FastSwipingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.InsistentAlarmClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleRefreshIntervalClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleStatisticClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ShowScheduleUpdateDialogClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlternativeScheduleUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetEngelsystemShiftsUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetScheduleRefreshInterval
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.AlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.AlternativeScheduleUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.EngelSystemUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleRefreshInterval
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleStatistic

internal class SettingsViewModel(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduleNextFetchUpdater: ScheduleNextFetchUpdater,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsStream
        .combine(appRepository.scheduleNextFetch) { settings, nextFetch ->
            SettingsUiState(
                settings = settings,
                nextFetch = nextFetch,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    private val mutableEffects = Channel<SettingsEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val activityResultKeys = mutableSetOf<String>()

    fun onViewEvent(event: SettingsEvent) = when (event) {
        ScheduleRefreshIntervalClicked -> navigateTo(ScheduleRefreshInterval)
        is SetScheduleRefreshInterval -> updateScheduleRefreshInterval(event.refreshInterval)
        ScheduleStatisticClicked -> navigateTo(ScheduleStatistic)
        AutoUpdateClicked -> toggleAutoUpdateEnabled()
        DeviceTimezoneClicked -> toggleUseDeviceTimeZoneEnabled()
        CustomizeNotificationsClicked -> launchNotificationSettingsScreen()
        AlternativeScheduleUrlClicked -> navigateTo(AlternativeScheduleUrl)
        is SetAlternativeScheduleUrl -> updateAlternativeScheduleUrl(event.url)
        AlternativeHighlightingClicked -> toggleAlternativeHighlightingEnabled()
        FastSwipingClicked -> toggleFastSwipingEnabled()
        ShowScheduleUpdateDialogClicked -> toggleShowScheduleUpdateDialogEnabled()
        AlarmToneClicked -> pickAlarmTone()
        is SetAlarmTone -> updateAlarmTone(event.alarmTone)
        InsistentAlarmClicked -> toggleInsistentAlarmsEnabled()
        AlarmTimeClicked -> navigateTo(AlarmTime)
        is SetAlarmTime -> updateAlarmTime(event.alarmTime)
        EngelsystemUrlClicked -> navigateTo(EngelSystemUrl)
        is SetEngelsystemShiftsUrl -> updateEngelsystemShiftsUrl(event.url)
    }

    private fun updateScheduleRefreshInterval(refreshInterval: Int) {
        settingsRepository.setScheduleRefreshInterval(refreshInterval)
        val isAutoUpdateEnabled = uiState.value.settings.isAutoUpdateEnabled
        scheduleNextFetchUpdater.update(isAutoUpdateEnabled)
        navigateBack()
    }

    private fun toggleAutoUpdateEnabled() {
        val isAutoUpdateEnabled = !uiState.value.settings.isAutoUpdateEnabled
        settingsRepository.setAutoUpdateEnabled(isAutoUpdateEnabled)
        scheduleNextFetchUpdater.update(isAutoUpdateEnabled)
    }

    private fun toggleUseDeviceTimeZoneEnabled() {
        val enabled = uiState.value.settings.isUseDeviceTimeZoneEnabled
        settingsRepository.setUseDeviceTimeZone(!enabled)
        updateActivityResult(USE_DEVICE_TIME_ZONE_UPDATED)
    }

    private fun launchNotificationSettingsScreen() {
        sendEffect(LaunchNotificationSettingsScreen)
    }

    private fun updateAlternativeScheduleUrl(url: String) {
        settingsRepository.setAlternativeScheduleUrl(url)
        updateActivityResult(SCHEDULE_URL_UPDATED)
        navigateBack()
    }

    private fun toggleAlternativeHighlightingEnabled() {
        val enabled = uiState.value.settings.isAlternativeHighlightingEnabled
        settingsRepository.setAlternativeHighlighting(!enabled)
        updateActivityResult(ALTERNATIVE_HIGHLIGHTING_UPDATED)
    }

    private fun toggleFastSwipingEnabled() {
        val enabled = uiState.value.settings.isFastSwipingEnabled
        settingsRepository.setFastSwiping(!enabled)
    }

    private fun toggleShowScheduleUpdateDialogEnabled() {
        val enabled = uiState.value.settings.isShowScheduleUpdateDialogEnabled
        settingsRepository.setShowScheduleUpdateDialog(!enabled)
    }

    private fun pickAlarmTone() {
        val currentAlarmTone = uiState.value.settings.alarmTone
        sendEffect(PickAlarmTone(currentAlarmTone))
    }

    private fun updateAlarmTone(alarmTone: Uri?) {
        settingsRepository.setAlarmTone(alarmTone)
    }

    private fun toggleInsistentAlarmsEnabled() {
        val enabled = uiState.value.settings.isInsistentAlarmsEnabled
        settingsRepository.setInsistentAlarms(!enabled)
    }

    private fun updateAlarmTime(alarmTime: Int) {
        settingsRepository.setAlarmTime(alarmTime)
        navigateBack()
    }

    private fun updateEngelsystemShiftsUrl(url: String) {
        settingsRepository.setEngelsystemShiftsUrl(url)
        updateActivityResult(ENGELSYSTEM_SHIFTS_URL_UPDATED)
        navigateBack()
    }

    private fun updateActivityResult(key: String) {
        activityResultKeys.add(key)
        sendEffect(SetActivityResult(activityResultKeys.toImmutableList()))
    }

    private fun navigateTo(destination: SettingsNavigationDestination) {
        sendEffect(NavigateTo(destination))
    }

    private fun navigateBack() {
        sendEffect(NavigateBack)
    }

    private fun sendEffect(event: SettingsEffect) {
        viewModelScope.launch {
            mutableEffects.send(event)
        }
    }
}
