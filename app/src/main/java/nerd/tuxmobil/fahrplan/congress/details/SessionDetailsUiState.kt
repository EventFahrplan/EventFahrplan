package nerd.tuxmobil.fahrplan.congress.details

import nerd.tuxmobil.fahrplan.congress.preferences.Settings

internal data class SessionDetailsUiState(
    val sessionDetailsState: SessionDetailsState = SessionDetailsState.Loading,
    val settings: Settings = Settings(),
)
