package nerd.tuxmobil.fahrplan.congress.settings

import nerd.tuxmobil.fahrplan.congress.preferences.Settings

internal data class SettingsUiState(
    val settings: Settings = Settings(),
)
