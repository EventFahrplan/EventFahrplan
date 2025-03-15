package nerd.tuxmobil.fahrplan.congress.details

import nerd.tuxmobil.fahrplan.congress.dataconverters.toRoom
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposition

class SelectedSessionParameterFactory(
    private val indoorNavigation: IndoorNavigation,
    private val feedbackUrlComposition: FeedbackUrlComposition,
    private val defaultEngelsystemRoomName: String,
) {

    fun createSelectedSessionParameter(session: Session): SelectedSessionParameter {
        val isFeedbackUrlEmpty = feedbackUrlComposition.getFeedbackUrl(session).isEmpty()
        val isEngelshift = session.roomName == defaultEngelsystemRoomName
        val supportsFeedback = !isFeedbackUrlEmpty && !isEngelshift
        val supportsIndoorNavigation = indoorNavigation.isSupported(session.toRoom())

        return SelectedSessionParameter(
            // Options menu
            isFlaggedAsFavorite = session.isHighlight,
            hasAlarm = session.hasAlarm,
            supportsFeedback = supportsFeedback,
            supportsIndoorNavigation = supportsIndoorNavigation,
        )
    }

}
