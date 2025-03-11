package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Room
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposition
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SelectedSessionParameterFactoryTest {

    private companion object {
        const val DEFAULT_ENGELSYSTEM_ROOM_NAME = "Engelsystem"
    }

    @Test
    fun `createSelectedSessionParameter returns all properties as false`() {
        val session = Session(
            sessionId = "S1",
            roomName = "Main hall",
            isHighlight = false,
            hasAlarm = false,
        )
        val result = SelectedSessionParameterFactory(
            indoorNavigation = UnsupportedIndoorNavigation,
            feedbackUrlComposition = UnsupportedFeedbackUrlComposer,
            defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        ).createSelectedSessionParameter(session)

        assertThat(result.isFlaggedAsFavorite).isFalse()
        assertThat(result.hasAlarm).isFalse()
        assertThat(result.supportsFeedback).isFalse()
        assertThat(result.supportsIndoorNavigation).isFalse()
    }

    @Test
    fun `createSelectedSessionParameter returns all properties as true`() {
        val session = Session(
            sessionId = "S1",
            roomName = "Main hall",
            isHighlight = true,
            hasAlarm = true,
        )
        val result = SelectedSessionParameterFactory(
            indoorNavigation = SupportedIndoorNavigation,
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        ).createSelectedSessionParameter(session)

        assertThat(result.isFlaggedAsFavorite).isTrue()
        assertThat(result.hasAlarm).isTrue()
        assertThat(result.supportsFeedback).isTrue()
        assertThat(result.supportsIndoorNavigation).isTrue()
    }

    @Test
    fun `createSelectedSessionParameter returns supportsFeedback as false when roomName is Engelsystem`() {
        val session = Session(
            sessionId = "S1",
            roomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        )
        val result = SelectedSessionParameterFactory(
            indoorNavigation = mock(),
            feedbackUrlComposition = SupportedFeedbackUrlComposer,
            defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        ).createSelectedSessionParameter(session)

        assertThat(result.supportsFeedback).isFalse()
    }

    @Test
    fun `createSelectedSessionParameter returns supportsFeedback as false when feedbackUrl is empty`() {
        val session = Session(
            sessionId = "S1",
            roomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        )
        val result = SelectedSessionParameterFactory(
            indoorNavigation = mock(),
            feedbackUrlComposition = UnsupportedFeedbackUrlComposer,
            defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
        ).createSelectedSessionParameter(session)

        assertThat(result.supportsFeedback).isFalse()

    }

    private object SupportedFeedbackUrlComposer : FeedbackUrlComposition {
        override fun getFeedbackUrl(session: Session) = "https://example.com"
    }

    private object UnsupportedFeedbackUrlComposer : FeedbackUrlComposition {
        override fun getFeedbackUrl(session: Session) = ""
    }

    private object SupportedIndoorNavigation : IndoorNavigation {
        override fun isSupported(room: Room) = true
        override fun getUri(room: Room) = "https://c3nav.foo/garden".toUri()
    }

    private object UnsupportedIndoorNavigation : IndoorNavigation {
        override fun isSupported(room: Room) = false
        override fun getUri(room: Room): Uri = Uri.EMPTY
    }

}
