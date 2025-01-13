package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test

class SessionUrlComposerTest {

    private companion object {

        const val FRAB_SESSION_URL_TEMPLATE =
                "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/%1\$s.html"

        const val PENTABARF_SESSION_URL_TEMPLATE =
                "https://fosdem.org/2018/schedule/event/%1\$s/"

        const val NO_URL = ""
        const val NO_SESSION_URL_TEMPLATE = ""
        const val NO_SERVER_BACKEND_TYPE = ""

        val PENTABARF_SESSION = Session(
            guid = "11111111-1111-1111-1111-111111117294",
            url = NO_URL,
            slug = "keynotes_welcome",
        )

        val FRAB_SESSION = Session(
            guid = "11111111-1111-1111-1111-111111119985",
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html",
            slug = "35c3-9985-opening_ceremony",
        )

        val PRETALX_SESSION = Session(
            guid = "11111111-1111-1111-1111-111111111132",
            url = "https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB",
            slug = "KDYQEB",
        )

        val ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL = Session(
            guid = "11111111-1111-1111-1111-111111117771",
            roomName = AppRepository.ENGELSYSTEM_ROOM_NAME,
            url = NO_URL,
        )

        val ENGELSYSTEM_SHIFT_SESSION_WITH_URL = Session(
            guid = "11111111-1111-1111-1111-111111117772",
            roomName = AppRepository.ENGELSYSTEM_ROOM_NAME,
            url = "https://helpful.to/the/angel",
        )

    }

    @Test
    fun `getSessionUrl returns URL if unknown backend is set`() {
        assertThat(SessionUrlComposer(NO_SESSION_URL_TEMPLATE, NO_SERVER_BACKEND_TYPE)
                .getSessionUrl(FRAB_SESSION)).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun `getSessionUrl returns Pentabarf URL if guid property and Pentabarf backend are set`() {
        assertThat(SessionUrlComposer(PENTABARF_SESSION_URL_TEMPLATE, ServerBackendType.PENTABARF.name)
                .getSessionUrl(PENTABARF_SESSION)).isEqualTo("https://fosdem.org/2018/schedule/event/keynotes_welcome/")
    }

    @Test
    fun `getSessionUrl returns Frab URL if url property and Frab backend are set`() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl(FRAB_SESSION)).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun `getSessionUrl returns Pretalx URL if url property and Frab backend are set`() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl(PRETALX_SESSION)).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun `getSessionUrl returns Pretalx URL if url property and Pretalx backend are set`() {
        assertThat(SessionUrlComposer(NO_SESSION_URL_TEMPLATE, ServerBackendType.PRETALX.name)
                .getSessionUrl(PRETALX_SESSION)).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun `getSessionUrl returns URL string if url property of sessionized shift is set`() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl(ENGELSYSTEM_SHIFT_SESSION_WITH_URL)).isEqualTo("https://helpful.to/the/angel")
    }

    @Test
    fun `getSessionUrl returns empty string if url property of sessionized shift is empty`() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl(ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL)).isEqualTo(NO_URL)
    }

}
