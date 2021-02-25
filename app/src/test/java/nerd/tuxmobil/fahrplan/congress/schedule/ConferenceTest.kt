package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test
import org.threeten.bp.ZoneOffset

class ConferenceTest {

    @Test
    fun `default values`() {
        val conference = Conference()
        assertThat(conference.firstSessionStartsAt).isEqualTo(0)
        assertThat(conference.lastSessionEndsAt).isEqualTo(0)
    }

    @Test
    fun `calculateTimeFrame throws exception if empty list is passed`() {
        try {
            createConference(*emptyList<Session>().toTypedArray())
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Empty list of sessions.")
        }
    }

    @Test
    fun `calculateTimeFrame with frab data spanning multiple days`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536504300000L, ZoneOffset.of("+02:00")) // 2018-09-09T16:45:00+02:00
        val (firstSessionStartsAt, lastSessionEndsAt, timeZoneOffset) = createConference(opening, closing)
        assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(lastSessionEndsAt).isEqualTo(17 * 60 + 15 - 2 * 60 + MINUTES_OF_ONE_DAY) // -> 17:15 -2h zone offset + day switch
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    @Test
    fun `calculateTimeFrame with frab data spanning a single day`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536336000000L, ZoneOffset.of("+02:00")) // 2018-09-07T18:00:00+02:00
        val (firstSessionStartsAt, lastSessionEndsAt, timeZoneOffset) = createConference(opening, closing)
        assertThat(firstSessionStartsAt).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(lastSessionEndsAt).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:30 -2h zone offset
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    @Test
    fun `calculateTimeFrame with frab data spanning a single day in non-chronological order`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536328800000L, ZoneOffset.of("+02:00")) // 2018-09-07T16:00:00+02:00
        val middle = createSession("Middle", duration = 20, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536336000000L, ZoneOffset.of("+02:00")) // 2018-09-07T18:00:00+02:00
        val (firstSessionStartsAt, lastSessionEndsAt, timeZoneOffset) = createConference(opening, closing, middle)
        assertThat(firstSessionStartsAt).isEqualTo(16 * 60 - 2 * 60) // 16:00h -2h zone offset = 14:00h
        assertThat(lastSessionEndsAt).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:30 -2h zone offset
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    private fun createConference(vararg sessions: Session) = Conference().apply {
        calculateTimeFrame(listOf(*sessions))
    }

    private fun createSession(sessionId: String, duration: Int, dateUtc: Long, timeZoneOffset: ZoneOffset) = Session(sessionId).apply {
        this.dateUTC = dateUtc
        this.duration = duration
        this.timeZoneOffset = timeZoneOffset
    }

}
