package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test

class SessionsTransformerTest {
    private val prioritizedRoomProvider = object : PrioritizedRoomProvider {
        override val prioritizedRooms = listOf("Ada", "Borg", "Clarke", "Dijkstra", "Eliza")
    }

    private val transformer = SessionsTransformer(prioritizedRoomProvider)

    @Test
    fun `ScheduleData_dayIndex contains proper day value`() {
        val scheduleData = transformer.transformSessions(dayIndex = 1, sessions = emptyList())

        assertThat(scheduleData.dayIndex).isEqualTo(1)
    }

    @Test
    fun `rooms are ordered by roomIndex`() {
        val sessionsInDatabase = listOf(
                createSession(sessionId = "11111111-1111-1111-1111-111111111111", roomName = "Four", roomIndex = 2342),
                createSession(sessionId = "22222222-2222-2222-2222-222222222222", roomName = "Two", roomIndex = 1),
                createSession(sessionId = "33333333-3333-3333-3333-333333333333", roomName = "One", roomIndex = 0),
                createSession(sessionId = "44444444-4444-4444-4444-444444444444", roomName = "Three", roomIndex = 4)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("One", "Two", "Three", "Four"))
    }

    @Test
    fun `prioritized rooms are first in list`() {
        val sessionsInDatabase = listOf(
                createSession(sessionId = "11111111-1111-1111-1111-111111111111", roomName = "Chaos-West Bühne", roomIndex = 11),
                createSession(sessionId = "22222222-2222-2222-2222-222222222222", roomName = "c-base", roomIndex = 12),
                createSession(sessionId = "33333333-3333-3333-3333-333333333333", roomName = "Eliza", roomIndex = 13),
                createSession(sessionId = "44444444-4444-4444-4444-444444444444", roomName = "Borg", roomIndex = 14),
                createSession(sessionId = "55555555-5555-5555-5555-555555555555", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Borg", "Eliza", "Chaos-West Bühne", "c-base"))
    }

    @Test
    fun `same roomIndex does not influence RoomData contents`() {
        val sessionsInDatabase = listOf(
                createSession(sessionId = "11111111-1111-1111-1111-111111111111", roomName = "Borg", roomIndex = 2),
                createSession(sessionId = "22222222-2222-2222-2222-222222222222", roomName = "Broken One", roomIndex = 2),
                createSession(sessionId = "33333333-3333-3333-3333-333333333333", roomName = "Broken Two", roomIndex = 2)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        with(scheduleData.roomDataList[0]) {
            assertThat(roomName).isEqualTo("Borg")
            assertThat(sessions[0].sessionId).isEqualTo("11111111-1111-1111-1111-111111111111")
        }
        with(scheduleData.roomDataList[1]) {
            assertThat(roomName).isEqualTo("Broken One")
            assertThat(sessions[0].sessionId).isEqualTo("22222222-2222-2222-2222-222222222222")
        }
        with(scheduleData.roomDataList[2]) {
            assertThat(roomName).isEqualTo("Broken Two")
            assertThat(sessions[0].sessionId).isEqualTo("33333333-3333-3333-3333-333333333333")
        }
    }

    private fun createSession(
            sessionId: String,
            roomName: String,
            roomIndex: Int,
            dateUTC: Long = 0
    ): Session {
        return Session(sessionId).apply {
            this.room = roomName
            this.roomIndex = roomIndex
            this.dateUTC = dateUTC
        }
    }
}
