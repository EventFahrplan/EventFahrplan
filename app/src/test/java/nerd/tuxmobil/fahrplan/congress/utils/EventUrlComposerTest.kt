package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class EventUrlComposerTest {

    companion object {

        private const val FRAB_EVENT_URL_TEMPLATE =
                "https://events.ccc.de/congress/2017/Fahrplan/events/%1\$s.html"

        private const val PENTABARF_EVENT_URL_TEMPLATE =
                "https://fosdem.org/2018/schedule/event/%1\$s/"

        private val PENTABARF_EVENT = Event("7294").apply {
            url = ""
            slug = "keynotes_welcome"
        }

        private val FRAB_EVENT = Event("9985").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
        }

        private val PRETALX_EVENT = Event("32").apply {
            url = "https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB"
            slug = "KDYQEB"
        }

    }

    @Test
    fun getEventUrlWithUnknownBackend() {
        try {
            EventUrlComposer(FRAB_EVENT, "", "").getEventUrl()
        } catch (e: NotImplementedError) {
            assertThat(e.message).isEqualTo("Unknown server backend type: ''")
        }
    }

    @Test
    fun getEventUrlWithPentabarfEventWithPentabarfBackend() {
        assertThat(EventUrlComposer(PENTABARF_EVENT, PENTABARF_EVENT_URL_TEMPLATE, ServerBackendType.PENTABARF.name)
                .getEventUrl()).isEqualTo("https://fosdem.org/2018/schedule/event/keynotes_welcome/")
    }

    @Test
    fun getEventUrlWithFrabEventWithFrabBackend() {
        assertThat(EventUrlComposer(FRAB_EVENT, FRAB_EVENT_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getEventUrl()).isEqualTo("https://events.ccc.de/congress/2017/Fahrplan/events/9985.html")
    }

    @Test
    fun getEventUrlWithPretalxEventWithPretalxBackend() {
        assertThat(EventUrlComposer(PRETALX_EVENT, "", ServerBackendType.PRETALX.name)
                .getEventUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

}

