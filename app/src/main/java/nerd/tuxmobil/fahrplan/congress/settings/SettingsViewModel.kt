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
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.SHOW_ON_LOCKSCREEN_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.SCHEDULE_URL_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.USE_DEVICE_TIME_ZONE_UPDATED
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.LaunchNotificationSettingsScreen
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.LaunchSessionAlarmChannelSettingsScreen
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.PickAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.AlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.AlternativeScheduleUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.EngelSystemUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleRefreshInterval
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleStatistic
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.SocialMediaHashtagsHandles
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AlarmTimeClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AlarmToneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AlternativeHighlightingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AlternativeScheduleUrlClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AutoUpdateClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.CustomizeNotificationsClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.DeviceTimezoneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.EngelsystemUrlClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.FastSwipingClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.InsistentAlarmClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ResetSocialMediaHashtagsHandles
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ScheduleChangesFavoritesFilterClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ScheduleRefreshIntervalClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ScheduleStatisticClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetAlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetAlternativeScheduleUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetEngelsystemShiftsUrl
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetScheduleRefreshInterval
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SetSocialMediaHashtagsHandles
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ShowOnLockscreenClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.ShowScheduleUpdateDialogClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.SocialMediaHashtagsHandlesClicked

internal class SettingsViewModel(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduleNextFetchUpdater: ScheduleNextFetchUpdater,
    /**
     * As of Android 8.0 (API level 26) the sound of a notification is defined by its notification
     * channel. The app cannot change the sound of an existing channel; only the user can.
     */
    private val isNotificationChannelSupported: Boolean,
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

    fun onViewEvent(viewEvent: SettingsViewEvent) = when (viewEvent) {
        ScheduleRefreshIntervalClicked -> navigateTo(ScheduleRefreshInterval)
        is SetScheduleRefreshInterval -> updateScheduleRefreshInterval(viewEvent.refreshInterval)
        ScheduleStatisticClicked -> navigateTo(ScheduleStatistic)
        AutoUpdateClicked -> toggleAutoUpdateEnabled()
        DeviceTimezoneClicked -> toggleUseDeviceTimeZoneEnabled()
        CustomizeNotificationsClicked -> launchNotificationSettingsScreen()
        AlternativeScheduleUrlClicked -> navigateTo(AlternativeScheduleUrl)
        is SetAlternativeScheduleUrl -> updateAlternativeScheduleUrl(viewEvent.url)
        AlternativeHighlightingClicked -> toggleAlternativeHighlightingEnabled()
        FastSwipingClicked -> toggleFastSwipingEnabled()
        ShowScheduleUpdateDialogClicked -> toggleShowScheduleUpdateDialogEnabled()
        ScheduleChangesFavoritesFilterClicked -> toggleScheduleChangesFavoritesFilterEnabled()
        ShowOnLockscreenClicked -> toggleShowOnLockscreenEnabled()
        AlarmToneClicked -> pickAlarmTone()
        is SetAlarmTone -> updateAlarmTone(viewEvent.alarmTone)
        InsistentAlarmClicked -> toggleInsistentAlarmsEnabled()
        AlarmTimeClicked -> navigateTo(AlarmTime)
        is SetAlarmTime -> updateAlarmTime(viewEvent.alarmTime)
        SocialMediaHashtagsHandlesClicked -> navigateTo(SocialMediaHashtagsHandles)
        is SetSocialMediaHashtagsHandles -> updateSocialMediaHashtagsHandles(viewEvent.handles)
        ResetSocialMediaHashtagsHandles -> resetSocialMediaHashtagsHandles()
        EngelsystemUrlClicked -> navigateTo(EngelSystemUrl)
        is SetEngelsystemShiftsUrl -> updateEngelsystemShiftsUrl(viewEvent.url)
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

    private fun toggleScheduleChangesFavoritesFilterEnabled() {
        val enabled = uiState.value.settings.isScheduleChangesFavoritesFilterEnabled
        settingsRepository.setScheduleChangesFavoritesFilter(!enabled)
    }

    private fun toggleShowOnLockscreenEnabled() {
        val enabled = uiState.value.settings.isShowOnLockscreenEnabled
        settingsRepository.setShowOnLockscreen(!enabled)
        updateActivityResult(SHOW_ON_LOCKSCREEN_UPDATED)
    }

    private fun pickAlarmTone() {
        if (isNotificationChannelSupported) {
            sendEffect(LaunchSessionAlarmChannelSettingsScreen)
            return
        }
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

    private fun updateSocialMediaHashtagsHandles(handles: String) {
        settingsRepository.setSocialMediaHashtagsHandles(handles)
        navigateBack()
    }

    private fun resetSocialMediaHashtagsHandles() {
        settingsRepository.resetSocialMediaHashtagsHandles()
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
