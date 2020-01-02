package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.utils.DateHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class ConferenceTest {

    @Test
    fun calculateTimeFrameFromFrabData() {
        val opening = Event("Opening").apply {
            dateUTC = 1536332400000L // 2018-09-07T17:00:00+02:00
            duration = 30
        }
        val closing = Event("Closing").apply {
            dateUTC = 1536504300000L // 2018-09-09T16:45:00+02:00
            duration = 30
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing), DateHelper::getMinuteOfDay)
            assertThat(firstEventStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
            assertThat(lastEventEndsAt).isEqualTo(1035 - 2 * 60 + Conference.ONE_DAY) // -> 17:15 -2h zone offset + day switch
        }
    }

    @Test
    fun calculateTimeFrameFromPentabarfData() {
        val opening = Event("Opening").apply {
            duration = 25
            relStartTime = 570 // 09:30
        }
        val closing = Event("Closing").apply {
            duration = 10
            relStartTime = 1070 // 17:50
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing), DateHelper::getMinuteOfDay)
            assertThat(firstEventStartsAt).isEqualTo(570) // 09:30
            assertThat(lastEventEndsAt).isEqualTo(1080) // 18:00
        }
    }
}
