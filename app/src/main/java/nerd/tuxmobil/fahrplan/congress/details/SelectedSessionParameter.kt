package nerd.tuxmobil.fahrplan.congress.details

/**
 * Intermediate model derived from the selected session.
 *
 * It is used to build the toolbar actions exposed via [SessionDetailsViewModel.uiState], which is
 * observed by the [SessionDetailsScreen].
 */
data class SelectedSessionParameter(
    // Options menu
    val isFlaggedAsFavorite: Boolean = false,
    val hasAlarm: Boolean = false,
    val supportsChaosflixExport: Boolean = false,
    val supportsFeedback: Boolean = false,
    val supportsIndoorNavigation: Boolean = false,
)
