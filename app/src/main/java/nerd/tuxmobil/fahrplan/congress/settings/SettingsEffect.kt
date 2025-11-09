package nerd.tuxmobil.fahrplan.congress.settings

import android.net.Uri
import kotlinx.collections.immutable.ImmutableList

internal sealed interface SettingsEffect {
    data class NavigateTo(val destination: SettingsNavigationDestination) : SettingsEffect
    data object NavigateBack : SettingsEffect
    data object LaunchNotificationSettingsScreen : SettingsEffect
    data class PickAlarmTone(val currentAlarmTone: Uri?) : SettingsEffect
    data class SetActivityResult(val keys: ImmutableList<String>) : SettingsEffect
}
