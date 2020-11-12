package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConferenceTest {

    @Test
    fun calculateTimeFrameFromFrabData_multipleDaySpan() {
        val opening = Session("Opening").apply {
            dateUTC = 1536332400000L // 2018-09-07T17:00:00+02:00
            duration = 30
        }
        val closing = Session("Closing").apply {
            dateUTC = 1536504300000L // 2018-09-09T16:45:00+02:00
            duration = 30
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing)) { dateUTC: Long -> Moment.ofEpochMilli(dateUTC).minuteOfDay }
            assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
            assertThat(lastSessionEndsAt).isEqualTo(1035 - 2 * 60 + MINUTES_OF_ONE_DAY) // -> 17:15 -2h zone offset + day switch
        }
    }

    @Test
    fun calculateTimeFrameFromFrabData_singleDay() {
        val opening = Session("Opening").apply {
            dateUTC = 1536332400000L // 2018-09-07T17:00:00+02:00
            duration = 30
        }
        val closing = Session("Closing").apply {
            dateUTC = 1536336000000L // 2018-09-07T18:00:00+02:00
            duration = 30
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing)) { dateUTC: Long -> Moment.ofEpochMilli(dateUTC).minuteOfDay }
            assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
            assertThat(lastSessionEndsAt).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:00 -2h zone offset
        }
    }

    @Test
    fun calculateTimeFrameFromPentabarfData() {
        val opening = Session("Opening").apply {
            duration = 25
            relStartTime = 570 // 09:30
        }
        val closing = Session("Closing").apply {
            duration = 10
            relStartTime = 1070 // 17:50
        }
        with(Conference()) {
            calculateTimeFrame(listOf(opening, closing)) { dateUTC: Long -> Moment.ofEpochMilli(dateUTC).minuteOfDay }
            assertThat(firstSessionStartsAt).isEqualTo(570) // 09:30
            assertThat(lastSessionEndsAt).isEqualTo(1080) // 18:00
        }
    }
}
