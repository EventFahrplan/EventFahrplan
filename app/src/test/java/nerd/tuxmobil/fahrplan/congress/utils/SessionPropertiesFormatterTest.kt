package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource

class SessionPropertiesFormatterTest {

    private companion object {

        const val DEFAULT_ENGELSYSTEM_ROOM_NAME = "Engelshifts"
        const val CUSTOM_ENGELSYSTEM_ROOM_NAME = "Trollshifts"

        @JvmStatic
        fun getLanguageTextData() = listOf(
            of("-formal", ""),
            of("German", "de"),
            of("german", "de"),
            of("Deutsch", "de"),
            of("deutsch", "de"),
            of("English", "en"),
            of("english", "en"),
            of("Englisch", "en"),
            of("englisch", "en"),
            of("German/English", "de/en"),
        )

    }

    private val formatter = SessionPropertiesFormatter(CompleteResourceResolver)

    @Test
    fun `getFormattedSessionId returns an empty string`() {
        assertThat(formatter.getFormattedSessionId("")).isEmpty()
    }

    @Test
    fun `getFormattedSessionId returns formatted session id string`() {
        assertThat(formatter.getFormattedSessionId("S4223")).isEqualTo("ID: S4223")
    }

    @Test
    fun `getFormattedLinks returns an empty string`() {
        val links = ""
        val expected = ""
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedLinks returns the given unmodified string`() {
        val links = "[VOC projects](https://www.voc.com/projects/)"
        val expected = "[VOC projects](https://www.voc.com/projects/)"
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedLinks returns a br separated string`() {
        val links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)"
        val expected = "[VOC projects](https://www.voc.com/projects/)<br>[POC](https://poc.com/QXut1XBymAk)"
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedUrl returns an empty string`() {
        val url = ""
        val expected = ""
        assertThat(formatter.getFormattedUrl(url)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedUrl returns an HTML formatted weblink`() {
        val url = "https://example.com/talk.html"
        val expected = """<a href="https://example.com/talk.html">https://example.com/talk.html</a>"""
        assertThat(formatter.getFormattedUrl(url)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedSpeakers returns empty string if speakers is empty`() {
        val session = createSession(speakers = emptyList())
        assertThat(formatter.getFormattedSpeakers(session)).isEmpty()
    }

    @Test
    fun `getFormattedSpeakers returns single speaker name if speakers contains one name`() {
        val session = createSession(speakers = listOf("Jane Doe"))
        assertThat(formatter.getFormattedSpeakers(session)).isEqualTo("Jane Doe")
    }

    @Test
    fun `getFormattedSpeakers returns speaker names if speakers contains multiple names`() {
        val session = createSession(speakers = listOf("Jane Doe", "John Doe"))
        assertThat(formatter.getFormattedSpeakers(session)).isEqualTo("Jane Doe, John Doe")
    }

    @Test
    fun `getFormattedTrackNameAndLanguageText returns track name if language is empty`() {
        val session = createSession(track = "Track", language = "")
        assertThat(formatter.getFormattedTrackNameAndLanguageText(session)).isEqualTo("Track")
    }

    @Test
    fun `getFormattedTrackNameAndLanguageText returns track name and language if track and language is not empty`() {
        val session = createSession(track = "Track", language = "de-formal")
        assertThat(formatter.getFormattedTrackNameAndLanguageText(session)).isEqualTo("Track [de]")
    }

    @Test
    fun `getFormattedTrackNameAndLanguageText returns language if track is empty and language is not empty`() {
        val session = createSession(track = "", language = "de-formal")
        assertThat(formatter.getFormattedTrackNameAndLanguageText(session)).isEqualTo("[de]")
    }

    @Test
    fun `getLanguageText returns empty string if language is empty`() {
        val session = createSession(language = "")
        assertThat(formatter.getLanguageText(session)).isEmpty()
    }

    @ParameterizedTest(name = """getLanguageText returns "{1}" if language "{0}"""")
    @MethodSource("getLanguageTextData")
    fun `getLanguageText returns two-letter language code`(language: String, expected: String) {
        val session = createSession(language = language)
        assertThat(formatter.getLanguageText(session)).isEqualTo(expected)
    }

    @Test
    fun `getRoomName returns empty string if room name is empty`() {
        val session = createSession(roomName = "")
        assertThat(
            formatter.getRoomName(
                roomName = session.roomName,
                defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
                customEngelsystemRoomName = CUSTOM_ENGELSYSTEM_ROOM_NAME,
            )
        ).isEmpty()
    }

    @Test
    fun `getRoomName returns original room name if room name does not match default Engelsystem room name`() {
        val session = createSession(roomName = "Hall 1")
        assertThat(
            formatter.getRoomName(
                roomName = session.roomName,
                defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
                customEngelsystemRoomName = CUSTOM_ENGELSYSTEM_ROOM_NAME,
            )
        ).isEqualTo("Hall 1")
    }

    @Test
    fun `getRoomName returns custom Engelsystem room name if room name matches default Engelsystem room name`() {
        val session = createSession(roomName = DEFAULT_ENGELSYSTEM_ROOM_NAME)
        assertThat(
            formatter.getRoomName(
                roomName = session.roomName,
                defaultEngelsystemRoomName = DEFAULT_ENGELSYSTEM_ROOM_NAME,
                customEngelsystemRoomName = CUSTOM_ENGELSYSTEM_ROOM_NAME,
            )
        ).isEqualTo(CUSTOM_ENGELSYSTEM_ROOM_NAME)
    }

    private fun createSession(
        speakers: List<String> = emptyList(),
        track: String = "",
        language: String = "",
        roomName: String = "",
    ) = Session(
        sessionId = "",
        speakers = speakers,
        track = track,
        language = language,
        roomName = roomName,
    )

    private object CompleteResourceResolver : ResourceResolving {
        override fun getString(id: Int, vararg formatArgs: Any): String {
            return when (id) {
                R.string.session_details_session_id -> "ID: ${formatArgs.first()}"
                else -> fail("Unknown string id : $id")
            }
        }

        override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): Nothing {
            throw NotImplementedError("Not needed for this test.")
        }

    }
}
