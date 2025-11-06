package nerd.tuxmobil.fahrplan.congress.settings

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.preferences.Settings

internal data class SettingsUiState(
    val isDevelopmentCategoryVisible: Boolean = BuildConfig.DEBUG,

    val settings: Settings = Settings(),
)
