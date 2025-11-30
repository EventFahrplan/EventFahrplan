package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.SimpleMessage

data class ErrorMessageUiState(
    val errorMessage: SimpleMessage,
    val shouldShowLong: Boolean,
)
