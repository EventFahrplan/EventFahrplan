package nerd.tuxmobil.fahrplan.congress.settings

import android.os.Build
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.preferences.Settings

internal data class SettingsUiState(
    val isDevelopmentCategoryVisible: Boolean = BuildConfig.DEBUG,
    val isNotificationSettingsVisible: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,

    val settings: Settings = Settings(),
)
