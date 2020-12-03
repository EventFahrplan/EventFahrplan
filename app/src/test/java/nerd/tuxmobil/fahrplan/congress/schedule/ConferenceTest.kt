package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test

class ConferenceTest {

    @Test
    fun `calculateTimeFrame with frab data spanning multiple days`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536504300000L) // 2018-09-09T16:45:00+02:00
        val (firstSessionStartsAt, lastSessionEndsAt) = createConference(opening, closing)
        assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(lastSessionEndsAt).isEqualTo(1035 - 2 * 60 + MINUTES_OF_ONE_DAY) // -> 17:15 -2h zone offset + day switch
    }

    @Test
    fun `calculateTimeFrame with frab data spanning a single day`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536336000000L) // 2018-09-07T18:00:00+02:00
        val (firstSessionStartsAt, lastSessionEndsAt) = createConference(opening, closing)
        assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(lastSessionEndsAt).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:00 -2h zone offset
    }

    private fun createConference(vararg sessions: Session) = Conference().apply {
        calculateTimeFrame(listOf(*sessions)) { dateUtc: Long ->
            Moment.ofEpochMilli(dateUtc).minuteOfDay
        }
    }

    private fun createSession(sessionId: String, duration: Int, dateUtc: Long) = Session(sessionId).apply {
        this.dateUTC = dateUtc
        this.duration = duration
    }

}
