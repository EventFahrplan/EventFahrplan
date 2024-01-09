package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionUrlComposerTest {

    private companion object {

        const val FRAB_SESSION_URL_TEMPLATE =
                "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/%1\$s.html"

        const val PENTABARF_SESSION_URL_TEMPLATE =
                "https://fosdem.org/2018/schedule/event/%1\$s/"

        const val NO_URL = ""
        const val NO_SESSION_URL_TEMPLATE = ""
        const val NO_SERVER_BACKEND_TYPE = ""

        val PENTABARF_SESSION = Session("7294").apply {
            url = NO_URL
            slug = "keynotes_welcome"
        }

        val FRAB_SESSION = Session("9985").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
        }

        val PRETALX_SESSION = Session("32").apply {
            url = "https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB"
            slug = "KDYQEB"
        }

        val ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL = Session("7771").apply {
            roomName = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = NO_URL
        }

        val ENGELSYSTEM_SHIFT_SESSION_WITH_URL = Session("7772").apply {
            roomName = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = "https://helpful.to/the/angel"
        }

    }

    @Test
    fun getSessionUrlWithUnknownBackend() {
        assertThat(SessionUrlComposer(NO_SESSION_URL_TEMPLATE, NO_SERVER_BACKEND_TYPE)
                .getSessionUrl(FRAB_SESSION)).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun getSessionUrlWithPentabarfSessionWithPentabarfBackend() {
        assertThat(SessionUrlComposer(PENTABARF_SESSION_URL_TEMPLATE, ServerBackendType.PENTABARF.name)
                .getSessionUrl(PENTABARF_SESSION)).isEqualTo("https://fosdem.org/2018/schedule/event/keynotes_welcome/")
    }

    @Test
    fun getSessionUrlWithFrabSessionWithFrabBackend() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl(FRAB_SESSION)).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun getSessionUrlWithPretalxSessionWithFrabBackend() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getSessionUrl(PRETALX_SESSION)).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getSessionUrlWithPretalxSessionWithPretalxBackend() {
        assertThat(SessionUrlComposer(NO_SESSION_URL_TEMPLATE, ServerBackendType.PRETALX.name)
                .getSessionUrl(PRETALX_SESSION)).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getSessionUrlWithShiftSessionWithUrl() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl(ENGELSYSTEM_SHIFT_SESSION_WITH_URL)).isEqualTo("https://helpful.to/the/angel")
    }

    @Test
    fun getSessionUrlWithShiftSessionWithoutUrl() {
        assertThat(SessionUrlComposer(FRAB_SESSION_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getSessionUrl(ENGELSYSTEM_SHIFT_SESSION_WITHOUT_URL)).isEqualTo(NO_URL)
    }

}
