package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoField

class ConferenceTest {

    @Test
    fun calculateTimeFrameFromFrabData() {
        // 2018-09-07T17:00:00+02:00 -> UTC: 1536332400000 milliseconds
        val opening = Event("Opening").apply {
            dateUTC = 1536332400000L // milliseconds
            duration = 30
        }
        // 2018-09-09T16:45:00+02:00 -> UTC: 1536504300000 milliseconds
        val closing = Event("Closing").apply {
            dateUTC = 1536504300000L
            duration = 30
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing), ::minutesOfDay)
            assertThat(firstEventStartsAt).isEqualTo(1020) // -> 17:00
            assertThat(lastEventEndsAt).isEqualTo(1035 + Conference.ONE_DAY) // -> 17:15 + day switch
        }
    }

    @Test
    fun calculateTimeFrameFromPentabarfData() {
        val opening = Event("Opening").apply {
            duration = 25
            relStartTime = 570 // -> 09:30
        }
        val closing = Event("Closing").apply {
            duration = 10
            relStartTime = 1070 // = 17:50
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing), ::minutesOfDay)
            assertThat(firstEventStartsAt).isEqualTo(570) // -> 09:30
            assertThat(lastEventEndsAt).isEqualTo(1080) // -> 18:00
        }
    }

    private fun minutesOfDay(dateUtc: Long): Int {
        val offset = ZoneOffset.ofHours(2) // according to the test data! +02:00
        val offsetDateTime = Instant.ofEpochMilli(dateUtc).atOffset(offset)
        return offsetDateTime.get(ChronoField.MINUTE_OF_DAY)
    }

}
