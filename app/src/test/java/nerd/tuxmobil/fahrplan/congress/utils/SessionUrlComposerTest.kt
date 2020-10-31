package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionUrlComposerTest {

    companion object {

        private const val FRAB_SESSION_URL_TEMPLATE =
                "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/%1\$s.html"

        private const val PENTABARF_SESSION_URL_TEMPLATE =
                "https://fosdem.org/2018/schedule/event/%1\$s/"

        private const val NO_URL = ""

        private val PENTABARF_SESSION = Session("11111111-1111-1111-1111-111111111111").apply {
            url = NO_URL
            slug = "keynotes_welcome"
        }

        private val FRAB_SESSION = Session("22222222-2222-2222-2222-222222222222").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
        }

        private val FRAB_SESSION_WITHOUT_URL = Session("33333333-3333-3333-3333-333333333333").apply {
            url = NO_URL
            slug = "35c3-1925-opening_ceremony"
        }

        private val PRETALX_SESSION = Session("44444444-4444-4444-4444-444444444444").apply {
            url = "https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB"
            slug = "KDYQEB"
        }

        private val ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL = Session("55555555-5555-5555-5555-555555555555").apply {
            room = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = NO_URL
        }

        private val ENGELSYSTEM_SHIFT_SESSION_WITH_URL = Session("66666666-6666-6666-6666-666666666666").apply {
            room = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = "https://helpful.to/the/angel"
        }

    }

    @Test
    fun getSessionUrlWithUnknownBackend() {
        try {
            SessionUrlComposer(FRAB_SESSION, "", "").getSessionUrl()
        } catch (e: NotImplementedError) {
            assertThat(e.message).isEqualTo("Unknown server backend type: ''")
        }
    }

    @Test
    fun getSessionUrlWithPentabarfSessionWithPentabarfBackend() {
        assertThat(SessionUrlComposer(PENTABARF_SESSION, PENTABARF_SESSION_URL_TEMPLATE, ServerBackendType.PENTABARF.name)
                .getSessionUrl()).isEqualTo("https://fosdem.org/2018/schedule/event/keynotes_welcome/")
    }

    @Test
    fun getSessionUrlWithFrabSessionWithoutUrl() {
        try {
            SessionUrlComposer(FRAB_SESSION_WITHOUT_URL, FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name).getSessionUrl()
        } catch (e: IllegalStateException) {
            assertThat(e.message).isEqualTo("Missing 'url' value for session ${FRAB_SESSION_WITHOUT_URL.sessionId}.")
        }
    }

    @Test
    fun getSessionUrlWithFrabSessionWithFrabBackend() {
        assertThat(SessionUrlComposer(FRAB_SESSION, FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl()).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun getSessionUrlWithPretalxSessionWithFrabBackend() {
        assertThat(SessionUrlComposer(PRETALX_SESSION, FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getSessionUrlWithPretalxSessionWithPretalxBackend() {
        assertThat(SessionUrlComposer(PRETALX_SESSION, "", ServerBackendType.PRETALX.name)
                .getSessionUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getSessionUrlWithShiftSessionWithUrl() {
        assertThat(SessionUrlComposer(ENGELSYSTEM_SHIFT_SESSION_WITH_URL, FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl()).isEqualTo("https://helpful.to/the/angel")
    }

    @Test
    fun getSessionUrlWithShiftSessionWithoutUrl() {
        assertThat(SessionUrlComposer(ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL, FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl()).isEqualTo("")
    }

}

