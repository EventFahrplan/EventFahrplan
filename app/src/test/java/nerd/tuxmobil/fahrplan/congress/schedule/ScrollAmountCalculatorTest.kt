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
                session = session,
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
                session = session,
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
                session = session,
                nowMoment = session.startsAt.minusMinutes(1),
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns 0 if conference starts now`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                session = session,
                nowMoment = session.startsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns 0 if first session is almost done`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                session = session,
                nowMoment = session.endsAt.minusMinutes(1),
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(0)
    }

    @Test
    fun `calculateScrollAmount returns end of session if first session is done`() {
        val session = createFirstSession()
        val scrollAmount = calculateScrollAmount(
                session = session,
                nowMoment = session.endsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(408)
    }

    @Test
    fun `calculateScrollAmount returns 408 for a session crossing the intra-day limit`() {
        val session = createLateSession()
        val scrollAmount = calculateScrollAmount(
                session = session,
                nowMoment = session.endsAt,
                currentDayIndex = session.dayIndex
        )
        assertThat(scrollAmount).isEqualTo(408)
    }

    private fun calculateScrollAmount(
            session: Session,
            nowMoment: Moment,
            currentDayIndex: Int,
            columnIndex: Int = COLUMN_INDEX
    ): Int {
        val sessions = listOf(session)
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

    private fun createLateSession() = createBaseSession("s2",
            Moment.ofEpochMilli(1583019000000L) // February 29, 2020 11:30:00 PM GMT
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

}
