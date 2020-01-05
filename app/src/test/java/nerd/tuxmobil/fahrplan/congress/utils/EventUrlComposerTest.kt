package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class EventUrlComposerTest {

    companion object {

        private const val FRAB_EVENT_URL_TEMPLATE =
                "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/%1\$s.html"

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

        private val ENGELSYSTEM_SHIFT_EVENT_WITHOUT_URL = Event("7771").apply {
            room = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = ""
        }

        private val ENGELSYSTEM_SHIFT_EVENT_WITH_URL = Event("7772").apply {
            room = AppRepository.ENGELSYSTEM_ROOM_NAME
            url = "https://helpful.to/the/angel"
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
                .getEventUrl()).isEqualTo("https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html")
    }

    @Test
    fun getEventUrlWithPretalxEventWithFrabBackend() {
        assertThat(EventUrlComposer(PRETALX_EVENT, FRAB_EVENT_URL_TEMPLATE, ServerBackendType.FRAB.name)
                .getEventUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getEventUrlWithPretalxEventWithPretalxBackend() {
        assertThat(EventUrlComposer(PRETALX_EVENT, "", ServerBackendType.PRETALX.name)
                .getEventUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB")
    }

    @Test
    fun getEventUrlWithShiftEventWithUrl() {
        assertThat(EventUrlComposer(ENGELSYSTEM_SHIFT_EVENT_WITH_URL, FRAB_EVENT_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getEventUrl()).isEqualTo("https://helpful.to/the/angel")
    }

    @Test
    fun getEventUrlWithShiftEventWithoutUrl() {
        assertThat(EventUrlComposer(ENGELSYSTEM_SHIFT_EVENT_WITHOUT_URL, FRAB_EVENT_URL_TEMPLATE, ServerBackendType.FRAB.name, setOf(AppRepository.ENGELSYSTEM_ROOM_NAME))
                .getEventUrl()).isEqualTo("")
    }

}

