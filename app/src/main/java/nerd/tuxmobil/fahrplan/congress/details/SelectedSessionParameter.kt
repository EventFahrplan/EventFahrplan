package nerd.tuxmobil.fahrplan.congress.details

/**
 * Payload of the observable [selectedSessionParameter][SessionDetailsViewModel.selectedSessionParameter]
 * property in the [SessionDetailsViewModel] which is observed by the [SessionDetailsFragment].
 */
data class SelectedSessionParameter(
    // Options menu
    val isFlaggedAsFavorite: Boolean,
    val hasAlarm: Boolean,
    val supportsFeedback: Boolean,
    val supportsIndoorNavigation: Boolean,
)
