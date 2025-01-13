package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class SessionsTransformerTest {

    @Test
    fun `ScheduleData_dayIndex contains proper day value`() {
        val transformer = SessionsTransformer(createRoomProvider())
        val scheduleData = transformer.transformSessions(dayIndex = 1, sessions = emptyList())

        assertThat(scheduleData.dayIndex).isEqualTo(1)
    }

    @Test
    fun `rooms are ordered by roomIndex`() {
        val transformer = SessionsTransformer(createRoomProvider())
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Four", roomIndex = 2342),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "Two", roomIndex = 1),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "One", roomIndex = 0),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Three", roomIndex = 4)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("One", "Two", "Three", "Four"))
    }

    @Test
    fun `empty prioritized rooms do not reorder the list`() {
        val transformer = SessionsTransformer(createRoomProvider(prioritizedRooms = emptyList()))
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Chaos-West Bühne", roomIndex = 11),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "c-base", roomIndex = 12),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Eliza", roomIndex = 13),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Borg", roomIndex = 14),
            createSession(guid = "11111111-1111-1111-1111-11111111111L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Chaos-West Bühne", "c-base", "Eliza", "Borg", "Ada"))
    }

    @Test
    fun `prioritized rooms are first in list`() {
        val transformer = SessionsTransformer(createRoomProvider())
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Chaos-West Bühne", roomIndex = 11),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "c-base", roomIndex = 12),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Eliza", roomIndex = 13),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Borg", roomIndex = 14),
            createSession(guid = "11111111-1111-1111-1111-11111111111L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Borg", "Eliza", "Chaos-West Bühne", "c-base"))
    }

    @Test
    fun `empty deprioritized rooms do not reorder the list`() {
        val transformer = SessionsTransformer(createRoomProvider(deprioritizedRooms = emptyList()))
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Zeppelin", roomIndex = 11),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "You", roomIndex = 12),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Erna", roomIndex = 13),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Bjoern", roomIndex = 14),
            createSession(guid = "11111111-1111-1111-1111-11111111111L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Zeppelin", "You", "Erna", "Bjoern"))
    }


    @Test
    fun `odd deprioritized rooms are last in list`() {
        val transformer = SessionsTransformer(createRoomProvider(deprioritizedRooms = listOf("Hamburg", "Frankfurt")))
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Zeppelin", roomIndex = 11),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "You", roomIndex = 12),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Erna", roomIndex = 13),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Bjoern", roomIndex = 14),
            createSession(guid = "11111111-1111-1111-1111-11111111111L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Zeppelin", "You", "Erna", "Bjoern"))
    }

    @Test
    fun `deprioritized rooms are last in list`() {
        val transformer = SessionsTransformer(createRoomProvider(deprioritizedRooms = listOf("You", "Zeppelin")))
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Zeppelin", roomIndex = 11),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "You", roomIndex = 12),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Erna", roomIndex = 13),
            createSession(guid = "11111111-1111-1111-1111-11111111111L3", roomName = "Bjoern", roomIndex = 14),
            createSession(guid = "11111111-1111-1111-1111-11111111111L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Erna", "Bjoern", "You", "Zeppelin"))
    }

    @Test
    fun `same roomIndex does not influence RoomData contents`() {
        val transformer = SessionsTransformer(createRoomProvider())
        val sessionsInDatabase = listOf(
            createSession(guid = "11111111-1111-1111-1111-11111111111L0", roomName = "Borg", roomIndex = 2),
            createSession(guid = "11111111-1111-1111-1111-11111111111L1", roomName = "Broken One", roomIndex = 2),
            createSession(guid = "11111111-1111-1111-1111-11111111111L2", roomName = "Broken Two", roomIndex = 2)
        )

        val scheduleData = transformer.transformSessions(dayIndex = 0, sessions = sessionsInDatabase)

        with(scheduleData.roomDataList[0]) {
            assertThat(roomName).isEqualTo("Borg")
            assertThat(sessions[0].guid).isEqualTo("11111111-1111-1111-1111-111111111110")
        }
        with(scheduleData.roomDataList[1]) {
            assertThat(roomName).isEqualTo("Broken One")
            assertThat(sessions[0].guid).isEqualTo("11111111-1111-1111-1111-111111111111")
        }
        with(scheduleData.roomDataList[2]) {
            assertThat(roomName).isEqualTo("Broken Two")
            assertThat(sessions[0].guid).isEqualTo("11111111-1111-1111-1111-111111111112")
        }
    }

    private fun createSession(
        guid: String,
        roomName: String,
        roomIndex: Int,
        dateUTC: Long = 0
    ) = Session(
        guid = guid,
        roomName = roomName,
        roomIndex = roomIndex,
        dateUTC = dateUTC,
    )

    private fun createRoomProvider(
        prioritizedRooms: List<String> = listOf("Ada", "Borg", "Clarke", "Dijkstra", "Eliza"),
        deprioritizedRooms: List<String> = emptyList()
    ): RoomProvider {
        return object : RoomProvider {
            override val prioritizedRooms = prioritizedRooms
            override val deprioritizedRooms = deprioritizedRooms
        }
    }

}
