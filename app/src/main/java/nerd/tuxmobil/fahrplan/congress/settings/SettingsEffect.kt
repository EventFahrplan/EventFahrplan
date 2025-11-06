package nerd.tuxmobil.fahrplan.congress.settings

import kotlinx.collections.immutable.ImmutableList

internal sealed interface SettingsEffect {
    data class SetActivityResult(val keys: ImmutableList<String>) : SettingsEffect
}
