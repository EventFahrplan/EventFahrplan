package nerd.tuxmobil.fahrplan.congress.settings

import kotlinx.collections.immutable.ImmutableList

internal sealed interface SettingsEffect {
    data class NavigateTo(val destination: SettingsNavigationDestination) : SettingsEffect
    data object NavigateBack : SettingsEffect
    data class SetActivityResult(val keys: ImmutableList<String>) : SettingsEffect
}
