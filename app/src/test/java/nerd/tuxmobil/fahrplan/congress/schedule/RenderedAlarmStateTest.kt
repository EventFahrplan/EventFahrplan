package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class RenderedAlarmStateTest {

    private val renderedAlarmState = RenderedAlarmState()

    @Test
    fun `findNewlyAddedAlarmSessionIds returns empty set for sessions without previous rendered state`() {
        val roomDataList = listOf(
            createRoomData(
                Session(sessionId = "session-1", hasAlarm = true)
            )
        )
        val sessionIds = renderedAlarmState.findNewlyAddedAlarmSessionIds(roomDataList)
        assertThat(sessionIds).isEmpty()
    }

    @Test
    fun `findNewlyAddedAlarmSessionIds returns session ids for alarms added since previous render`() {
        renderedAlarmState.remember(
            listOf(
                createRoomData(
                    Session(sessionId = "session-1", hasAlarm = false),
                    Session(sessionId = "session-2", hasAlarm = true),
                )
            )
        )
        val roomDataList = listOf(
            createRoomData(
                Session(sessionId = "session-1", hasAlarm = true),
                Session(sessionId = "session-2", hasAlarm = true),
            )
        )
        val sessionIds = renderedAlarmState.findNewlyAddedAlarmSessionIds(roomDataList)
        assertThat(sessionIds).containsExactly("session-1")
    }

    @Test
    fun `findNewlyAddedAlarmSessionIds returns empty set after clear`() {
        renderedAlarmState.remember(
            listOf(
                createRoomData(
                    Session(sessionId = "session-1", hasAlarm = false)
                )
            )
        )
        renderedAlarmState.clear()
        val roomDataList = listOf(
            createRoomData(
                Session(sessionId = "session-1", hasAlarm = true)
            )
        )
        val sessionIds = renderedAlarmState.findNewlyAddedAlarmSessionIds(roomDataList)
        assertThat(sessionIds).isEmpty()
    }

    private fun createRoomData(vararg sessions: Session) = RoomData(
        roomName = "room",
        sessions = sessions.toList(),
    )

}
