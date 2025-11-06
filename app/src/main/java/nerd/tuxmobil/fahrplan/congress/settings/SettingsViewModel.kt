package nerd.tuxmobil.fahrplan.congress.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.DeviceTimezoneClicked

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

    fun onViewEvent(event: SettingsEvent) {
        handleEvent(event)
    }

    private fun handleEvent(event: SettingsEvent) = when (event) {
        DeviceTimezoneClicked -> toggleUseDeviceTimeZoneEnabled()
    }

    private fun toggleUseDeviceTimeZoneEnabled() {
        val enabled = uiState.value.settings.isUseDeviceTimeZoneEnabled
        settingsRepository.setUseDeviceTimeZone(!enabled)
    }
}
