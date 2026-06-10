package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.BOX_HEIGHT_MULTIPLIER
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.FIFTEEN_MINUTES
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset

class ScrollAmountCalculatorTest {

    private companion object {
        const val BOX_HEIGHT = 34 // Pixel 2 portrait mode
        const val COLUMN_INDEX = 0
    }

    @Test
    fun `calculateScrollAmount returns 0 if room index preceeds the valid column indices`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.startsAt,
                currentDayIndex = session.dayIndex,
                columnIndex = -1
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns 0 if room index exceeds the valid column indices`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.startsAt,
                currentDayIndex = session.dayIndex,
                columnIndex = COLUMN_INDEX + 1
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns 0 if conference has not started but it will today`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.startsAt.minusMinutes(1),
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns start of second session with timezone offset data`() {
        val s1 = createFirstTimezoneOffsetSession()
        val s2 = createSecondTimezoneOffsetSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(s1, s2),
                nowMoment = s2.startsAt, // 10:00 UTC -> 816 UTC minutes
                currentDayIndex = s1.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(816)
    }

    @Test
    fun `calculateScrollAmount returns 0 if conference starts now`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.startsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns 0 if first session is almost done`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.endsAt.minusMinutes(1),
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns end of session if first session is done`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.endsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(408)
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(session.startsAt.minutesUntil(session.endsAt)))
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(session.duration.toWholeMinutes()))
    }

    @Test
    fun `calculateScrollAmount returns start of 2nd session`() {
        val s1 = createFirstSession()
        val s2 = createSecondSession()
        val scrollAmount = calculateScrollAmount(
            sessions = listOf(s1, s2),
            nowMoment = s2.startsAt,
            currentDayIndex = s1.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(816)
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(s1.startsAt.minutesUntil(s2.startsAt)))
    }

    @Test
    fun `calculateScrollAmount returns start of late session`() {
        val s1 = createFirstSession()
        val s2 = createLateSession()
        val scrollAmount = calculateScrollAmount(
            sessions = listOf(s1, s2),
            nowMoment = s2.startsAt,
            currentDayIndex = s1.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(6324)
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(s1.startsAt.minutesUntil(s2.startsAt)))
    }

    @Test
    fun `calculateScrollAmount returns end of late session`() {
        val s1 = createFirstSession()
        val s2 = createLateSession()
        val scrollAmount = calculateScrollAmount(
            sessions = listOf(s1, s2),
            nowMoment = s2.endsAt,
            currentDayIndex = s1.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(6732)
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(s1.startsAt.minutesUntil(s2.endsAt)))
    }

    @Test
    fun `calculateScrollAmount returns start of 2nd session one minute before it ends the next day`() {
        val s1 = createFirstSession()   // 08:00..09:00 -> 480..540   -> 0..408
        val s2 = createLateSession()    // 23:30..00:30 -> 1410..1470 -> 6324..6732
        val s3 = createNextDaySession() // 02:00..03:00 -> 1560..1620 -> 7344..7752
        val scrollAmount = calculateScrollAmount(
            sessions = listOf(s1, s2, s3),
            nowMoment = s2.endsAt.minusMinutes(1), // March 1, 2020 00:29:00 AM GMT
            currentDayIndex = s1.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(6324)
        assertThat(scrollAmount).isEqualTo(scrollAmountFor(s1.startsAt.minutesUntil(s2.startsAt)))
    }

    private fun scrollAmountFor(minutes: Long) = minutes / FIFTEEN_MINUTES * BOX_HEIGHT * BOX_HEIGHT_MULTIPLIER

    @Test
    fun `calculateScrollAmount returns 408 for a session crossing the intra-day limit`() {
        val session = createLateSession()
        val scrollAmount = calculateScrollAmount(
                sessions = listOf(session),
                nowMoment = session.endsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(408)
    }

    private fun calculateScrollAmount(
            sessions: List<Session>,
            nowMoment: Moment,
            currentDayIndex: Int,
            columnIndex: Int = COLUMN_INDEX
    ): Int {
        val session = sessions.first()
        val roomData = RoomData(roomName = session.roomName, sessions = sessions)
        val scheduleData = ScheduleData(dayIndex = session.dayIndex, roomDataList = listOf(roomData))
        val conference = Conference.ofSessions(sessions)
        val dateInfo = DateInfo(dayIndex = session.dayIndex, date = Moment.parseDate(session.dateText))
        val dateInfos = DateInfos().apply { add(dateInfo) }
        return ScrollAmountCalculator(NoLogging).calculateScrollAmount(conference, dateInfos, scheduleData, nowMoment, currentDayIndex, BOX_HEIGHT, columnIndex)
    }

    private fun createFirstSession() = createBaseSession("s1",
            Moment.ofEpochMilli(1582963200000L) // February 29, 2020 08:00:00 AM GMT
    )

    private fun createSecondSession() = createBaseSession("s2",
            Moment.ofEpochMilli(1582970400000L) // February 29, 2020 10:00:00 AM GMT
    )

    private fun createLateSession() = createBaseSession("s3",
            Moment.ofEpochMilli(1583019000000L) // February 29, 2020 11:30:00 PM GMT
    )

    private fun createNextDaySession() = createBaseSession("s4",
            Moment.ofEpochMilli(1583028000000L) // March 1, 2020 02:00:00 AM GMT
    )

    private fun createBaseSession(sessionId: String, moment: Moment) = Session(
        sessionId = sessionId,
        dayIndex = 0,
        dateText = moment.toZonedDateTime(ZoneOffset.UTC).toLocalDate().toString(),
        dateUTC = moment.toMilliseconds(),
        startTime = Duration.ofMinutes(moment.minuteOfDay),
        duration = Duration.ofMinutes(60),
        roomName = "Main hall",
    )

    private fun createFirstTimezoneOffsetSession() = createTimezoneOffsetSession("first legacy",
        Moment.ofEpochMilli(1582963200000L), // February 29, 2020 08:00:00 AM GMT
        60 * 10, // simulating +02:00 timezone offset as in JSON/XML data
    )

    private fun createSecondTimezoneOffsetSession() = createTimezoneOffsetSession("second legacy",
        Moment.ofEpochMilli(1582970400000L), // February 29, 2020 10:00:00 AM GMT
        60 * 12, // simulating +02:00 timezone offset as in JSON/XML data
    )

    // In JSON/XML the "start" property value is independent of the UTC value in the "date" property.
    // The "start" value comes in the local timezone of the event.
    private fun createTimezoneOffsetSession(sessionId: String, moment: Moment, startTime: Int) = Session(
        sessionId = sessionId,
        dayIndex = 0,
        dateText = moment.toZonedDateTime(ZoneOffset.UTC).toLocalDate().toString(),
        dateUTC = moment.toMilliseconds(),
        startTime = Duration.ofMinutes(startTime),
        duration = Duration.ofMinutes(60),
        roomName = "Main hall",
    )

}
