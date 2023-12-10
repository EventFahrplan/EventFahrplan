package nerd.tuxmobil.fahrplan.congress.details

/**
 * Payload of the observable [selectedSessionParameter][SessionDetailsViewModel.selectedSessionParameter]
 * property in the [SessionDetailsViewModel] which is observed by the [SessionDetailsFragment].
 */
data class SelectedSessionParameter(

    // Details content
    val sessionId: String,

    val hasDateUtc: Boolean,
    val formattedZonedDateTimeShort: String,
    val formattedZonedDateTimeLong: String,

    val title: String,
    val subtitle: String,
    val speakerNames: String,
    val speakersCount: Int,
    val formattedAbstract: String,
    val abstract: String,
    val description: String,
    val formattedDescription: String,
    val roomName: String,
    val track: String,

    val hasLinks: Boolean,
    val formattedLinks: String,
    val hasWikiLinks: Boolean,
    val sessionLink: String,

    // Options menu
    val isFlaggedAsFavorite: Boolean,
    val hasAlarm: Boolean,
    val supportsFeedback: Boolean,
    val supportsIndoorNavigation: Boolean,

    )
