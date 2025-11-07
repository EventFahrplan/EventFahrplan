package nerd.tuxmobil.fahrplan.congress.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.USE_DEVICE_TIME_ZONE_UPDATED
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.DeviceTimezoneClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.ScheduleStatisticClicked
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleStatistic

internal class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsStream
        .map { settings ->
            SettingsUiState(settings = settings)
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
        ScheduleStatisticClicked -> navigateTo(ScheduleStatistic)
        DeviceTimezoneClicked -> toggleUseDeviceTimeZoneEnabled()
    }

    private fun toggleUseDeviceTimeZoneEnabled() {
        val enabled = uiState.value.settings.isUseDeviceTimeZoneEnabled
        settingsRepository.setUseDeviceTimeZone(!enabled)
        updateActivityResult(USE_DEVICE_TIME_ZONE_UPDATED)
    }

    private fun updateActivityResult(key: String) {
        activityResultKeys.add(key)
        sendEffect(SetActivityResult(activityResultKeys.toImmutableList()))
    }

    private fun navigateTo(destination: SettingsNavigationDestination) {
        sendEffect(NavigateTo(destination))
    }

    private fun sendEffect(event: SettingsEffect) {
        viewModelScope.launch {
            mutableEffects.send(event)
        }
    }
}
