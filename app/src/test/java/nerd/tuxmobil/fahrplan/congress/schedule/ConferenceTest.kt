package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset

class ConferenceTest {

    @Test
    fun `ofSessions throws exception if empty list is passed`() {
        try {
            createConference(*emptyList<Session>().toTypedArray())
            fail("Expect an IllegalArgumentException to be thrown.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Empty list of sessions.")
        }
    }

    @Test
    fun `ofSession returns time range for one virtual conference day spanning two natural days`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1703671200000, ZoneOffset.of("+00:00")) // 2023-12-27T10:00:00+00:00
        val dancing = createSession("Dancing", duration = 60, dateUtc = 1703736000000, ZoneOffset.of("+00:00")) // 2023-12-28T04:00:00+00:00
        with(createConference(opening, dancing)) {
            assertThat(firstSessionStartsAt).isEqualTo(Moment.ofEpochMilli(1703671200000)) // 2023-12-27T10:00:00+00:00
            assertThat(lastSessionEndsAt).isEqualTo(Moment.ofEpochMilli(1703739600000)) // 2023-12-28T05:00:00+00:00
            assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+00:00"))
            assertThat(spansMultipleDays).isTrue()
        }
    }

    @Test
    fun `ofSessions with frab data spanning multiple days`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536504300000L, ZoneOffset.of("+02:00")) // 2018-09-09T16:45:00+02:00
        val (timeFrame, timeZoneOffset, spansMultipleDays) = createConference(opening, closing)
        val firstSessionStartsAtMinutes = timeFrame.start.minuteOfDay
        val minutesToAdd = if (spansMultipleDays) MINUTES_OF_ONE_DAY else 0
        val lastSessionEndsAtMinutes = timeFrame.endInclusive.minuteOfDay + minutesToAdd
        assertThat(firstSessionStartsAtMinutes).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(lastSessionEndsAtMinutes).isEqualTo(17 * 60 + 15 - 2 * 60 + MINUTES_OF_ONE_DAY) // -> 17:15 -2h zone offset + day switch
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    @Test
    fun `ofSessions with frab data spanning from winter to summer time`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1616857200000L, ZoneOffset.of("+01:00")) // 2021-03-27T16:00:00+01:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1616940000000L, ZoneOffset.of("+02:00")) // 2021-03-28T16:00:00+02:00
        val (timeFrame, timeZoneOffset, spansMultipleDays) = createConference(opening, closing)
        val firstSessionStartsAtMinutes = timeFrame.start.minuteOfDay
        val minutesToAdd = if (spansMultipleDays) MINUTES_OF_ONE_DAY else 0
        val lastSessionEndsAtMinutes = timeFrame.endInclusive.minuteOfDay + minutesToAdd
        assertThat(firstSessionStartsAtMinutes).isEqualTo(16 * 60 - 1 * 60) // 16:00h -1h zone offset = 15:00
        assertThat(lastSessionEndsAtMinutes).isEqualTo(16 * 60 - 2 * 60 + 30 + MINUTES_OF_ONE_DAY) // -> 16:30 -2h zone offset + day switch = 14:30
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+01:00"))
    }

    @Test
    fun `ofSessions with frab data spanning from summer to winter time`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1635602400000L, ZoneOffset.of("+02:00")) // 2021-10-30T16:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1635692400000L, ZoneOffset.of("+01:00")) // 2021-10-31T16:00:00+01:00
        val (timeFrame, timeZoneOffset, spansMultipleDays) = createConference(opening, closing)
        val firstSessionStartsAtMinutes = timeFrame.start.minuteOfDay
        val minutesToAdd = if (spansMultipleDays) MINUTES_OF_ONE_DAY else 0
        val lastSessionEndsAtMinutes = timeFrame.endInclusive.minuteOfDay + minutesToAdd
        assertThat(firstSessionStartsAtMinutes).isEqualTo(16 * 60 - 2 * 60) // 16:00h -1h zone offset = 14:00
        assertThat(lastSessionEndsAtMinutes).isEqualTo(16 * 60 - 1 * 60 + 30 + MINUTES_OF_ONE_DAY) // -> 16:30 -1h zone offset + day switch = 15:30
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00")) // spanning is not supported yet, see Conference class
    }

    @Test
    fun `ofSessions with frab data spanning a single day`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536336000000L, ZoneOffset.of("+02:00")) // 2018-09-07T18:00:00+02:00
        val (timeFrame, timeZoneOffset, _) = createConference(opening, closing)
        assertThat(timeFrame.start.minuteOfDay).isEqualTo(17 * 60 - 2 * 60) // 17:00h -2h zone offset = 15:00h
        assertThat(timeFrame.endInclusive.minuteOfDay).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:30 -2h zone offset
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    @Test
    fun `ofSessions with frab data spanning a single day in non-chronological order`() {
        val opening = createSession("Opening", duration = 30, dateUtc = 1536328800000L, ZoneOffset.of("+02:00")) // 2018-09-07T16:00:00+02:00
        val middle = createSession("Middle", duration = 20, dateUtc = 1536332400000L, ZoneOffset.of("+02:00")) // 2018-09-07T17:00:00+02:00
        val closing = createSession("Closing", duration = 30, dateUtc = 1536336000000L, ZoneOffset.of("+02:00")) // 2018-09-07T18:00:00+02:00
        val (timeFrame, timeZoneOffset, _) = createConference(opening, closing, middle)
        assertThat(timeFrame.start.minuteOfDay).isEqualTo(16 * 60 - 2 * 60) // 16:00h -2h zone offset = 14:00h
        assertThat(timeFrame.endInclusive.minuteOfDay).isEqualTo(18 * 60 + 30 - 2 * 60) // -> 18:30 -2h zone offset
        assertThat(timeZoneOffset).isEqualTo(ZoneOffset.of("+02:00"))
    }

    private fun createConference(vararg sessions: Session) = Conference.ofSessions(sessions.toList())

    private fun createSession(sessionId: String, duration: Int, dateUtc: Long, timeZoneOffset: ZoneOffset) = Session(sessionId).apply {
        this.dateUTC = dateUtc
        this.duration = duration
        this.timeZoneOffset = timeZoneOffset
    }

}
