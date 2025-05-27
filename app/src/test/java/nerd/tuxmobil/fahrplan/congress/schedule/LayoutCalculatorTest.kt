package nerd.tuxmobil.fahrplan.congress.schedule

import android.widget.LinearLayout
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class LayoutCalculatorTest {
    private val conferenceDate = "2020-03-30"
    private var sessionId = 0
    private val layoutCalculator = LayoutCalculator(standardHeight = 1, logging = NoLogging)

    private fun createSession(
        date: String? = null,
        startTime: Int = 0,
        duration: Int = 0,
    ): Session {
        var session = Session((sessionId++).toString(), duration = duration)

        if (date == null) {
            session = session.copy(relativeStartTime = startTime)
        } else {
            val dateUTC = Moment.parseDate(date).plusMinutes(startTime.toLong())
            session = session.copy(dateUTC = dateUTC.toMilliseconds())
        }

        return session
    }

    @Test
    fun `calculateLayoutParams for single session returns margins 0`() {
        val sessions = listOf(createSession())
        val conference = Conference.ofSessions(sessions)
        val roomData = sessions.toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val sessionParams = layoutParams[sessions.first().sessionId]

        assertMargins(sessionParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for single UTC session sets top margin 0 (its the first session in all rooms, so on the top)`() {
        val startTime = 10 * 60 // 10:00am
        val sessions = listOf(createSession(date = conferenceDate, startTime = startTime))
        val conference = Conference.ofSessions(sessions)
        val roomData = sessions.toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val sessionParams = layoutParams[sessions.first().sessionId]

        assertMargins(sessionParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for consecutive session sets margins based on gap duration`() {
        val startTime1 = 10 * 60 // 10:00am
        val duration1 = 45
        val gapMinutes = 15
        val startTime2 = startTime1 + duration1 + gapMinutes // 11:00am

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val sessions = listOf(session1, session2)
        val conference = Conference.ofSessions(sessions)
        val roomData = sessions.toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val session1Params = layoutParams[session1.sessionId]
        val session2Params = layoutParams[session2.sessionId]

        assertMargins(session1Params, 0, gapMinutes)
        assertMargins(session2Params, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for consecutive session in another room sets top margin based on conference day start`() {
        /*
                         room 1             room 2
                   +---------------------------------------+
            10:00  +-------------------+                   |  +
                   |                   |                   |  |
                   |     session 1     |                   |  |
                   |                   |                   |  | marginTop
            10:45  +-------------------+                   |  |
                   |                   |                   |  |
            11:00  |                   +-------------------+  +
                   |                   |                   |
                   |                   |    session 2      |
                   |                   |                   |

        * session 2 follows directly session 1, but in another room, hence the margin includes height of session 1.
        */
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 + 15 // 11:00am

        val session1 = createSession(date = conferenceDate, startTime = startTime1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val conference = Conference.ofSessions(listOf(session1, session2))
        val roomData = listOf(session2).toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val session2Params = layoutParams[session2.sessionId]
        val gapMinutes = 60

        assertMargins(session2Params, gapMinutes, 0)
    }

    @Test
    fun `calculateLayoutParams consecutive session after midnight in another room`() {
        val duration1 = 45
        val startTime1 = 23 * 60 // 11:00pm
        val startTime2 = startTime1 + duration1 + 20 // 00:05am, next day

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val sessionsInRoom1 = listOf(session1)
        val sessionsInRoom2 = listOf(session2)
        val conference = Conference.ofSessions(sessionsInRoom1 + sessionsInRoom2)
        val roomData1 = sessionsInRoom1.toRoomData()
        val roomData2 = sessionsInRoom2.toRoomData()

        val layoutParamsRoom1 = layoutCalculator.calculateLayoutParams(roomData1, conference)
        val layoutParamsRoom2 = layoutCalculator.calculateLayoutParams(roomData2, conference)
        val session1Params = layoutParamsRoom1[session1.sessionId]
        val session2Params = layoutParamsRoom2[session2.sessionId]
        val gapMinutes = 5 + 60 // 5 minutes in new day. 60 minutes on previous day, from session1, which starts at 11am

        assertMargins(session1Params, 0, 0)
        assertMargins(session2Params, gapMinutes, 0)
    }

    @Test
    fun `calculateLayoutParams consecutive session after midnight in same room`() {
        val duration1 = 45
        val startTime1 = 23 * 60 // 11:00pm
        val startTime2 = startTime1 + duration1 + 30 // 00:15am, next day

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val sessions = listOf(session1, session2)
        val conference = Conference.ofSessions(sessions)
        val roomData = sessions.toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val session1Params = layoutParams[session1.sessionId]
        val session2Params = layoutParams[session2.sessionId]
        val gapMinutes = 30

        assertMargins(session1Params, 0, gapMinutes)
        assertMargins(session2Params, 0, 0)
    }

    @Test
    fun `calculateLayoutParams overlapping session in same room - should cut first session duration to match next session start`() {
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 - 10 // 10:35am (10 minutes overlap)

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val sessions = listOf(session1, session2)
        val conference = Conference.ofSessions(sessions)
        val roomData = sessions.toRoomData()

        val layoutParams = layoutCalculator.calculateLayoutParams(roomData, conference)
        val session1Params = layoutParams[session1.sessionId]
        val session2Params = layoutParams[session2.sessionId]

        assertMargins(session1Params, 0, 0)
        assertMargins(session2Params, 0, 0)
    }

    @Test
    fun `calculateLayoutParams overlapping session in another room - should not cut any session`() {
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 - 10 // 10:35am (10 minutes overlap)

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2)
        val sessionsInRoom1 = listOf(session1)
        val sessionsInRoom2 = listOf(session2)
        val conference = Conference.ofSessions(sessionsInRoom1 + sessionsInRoom2)
        val roomData1 = sessionsInRoom1.toRoomData()
        val roomData2 = sessionsInRoom2.toRoomData()

        val layoutParamsRoom1 = layoutCalculator.calculateLayoutParams(roomData1, conference)
        val layoutParamsRoom2 = layoutCalculator.calculateLayoutParams(roomData2, conference)
        val session1Params = layoutParamsRoom1[session1.sessionId]
        val session2Params = layoutParamsRoom2[session2.sessionId]

        assertMargins(session1Params, 0, 0)
        assertMargins(session2Params, 35, 0)
    }

    @Test
    fun `fixOverlappingSessions decreases the duration of a session with an overlapping successor`() {
        val duration1 = 45
        val duration2 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 - 10 // 10:35am (10 minutes overlap)

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2, duration = duration2)

        val updatedSession1 = layoutCalculator.fixOverlappingSessions(session1, session2)
        assertThat(session1.duration).isEqualTo(45) // not mutated
        assertThat(updatedSession1.duration).isEqualTo(35) // gets cut
        assertThat(session2.duration).isEqualTo(45) // not mutated
    }

    @Test
    fun `fixOverlappingSessions keeps the duration of a session with a consecutive successor`() {
        val duration1 = 45
        val duration2 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 // 10:45am (no overlap)

        val session1 = createSession(date = conferenceDate, startTime = startTime1, duration = duration1)
        val session2 = createSession(date = conferenceDate, startTime = startTime2, duration = duration2)

        val updatedSession1 = layoutCalculator.fixOverlappingSessions(session1, session2)
        assertThat(session1.duration).isEqualTo(45) // not mutated
        assertThat(updatedSession1.duration).isEqualTo(45) // stays the same
        assertThat(session2.duration).isEqualTo(45) // not mutated
    }

    private fun assertMargins(sessionParams: LinearLayout.LayoutParams?, top: Int, bottom: Int) {
        assertThat(sessionParams!!).isNotNull()
        assertThat(sessionParams.topMargin).isEqualTo(layoutCalculator.calculateDisplayDistance(top))
        assertThat(sessionParams.bottomMargin).isEqualTo(layoutCalculator.calculateDisplayDistance(bottom))
    }

    private fun List<Session>.toRoomData() = RoomData(roomName = "irrelevant", sessions = this)
}

