package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ScheduleDataTest {

    private val actualSession = Session(guid = "11111111-1111-1111-1111-111111111142", roomName = "Room1")
    private val oddSession = Session(guid = "11111111-1111-1111-1111-111111111178", roomName = "Room78")

    @Test
    fun `roomDataList without rooms and sessions`() {
        val data = scheduleDataOf(emptyList())
        assertThat(data.roomCount).isEqualTo(0)
        assertThat(data.roomNames).isEqualTo(emptyList<String>())
        assertThat(data.allSessions).isEqualTo(emptyList<Session>())
        assertThat(data.findSession(actualSession.guid)).isNull()
        assertThat(data.findRoomIndex(actualSession)).isEqualTo(ScheduleData.UNKNOWN_ROOM_INDEX)
    }

    @Test
    fun `roomDataList with one room without sessions`() {
        val emptyRoom = listOf(emptyRoomOf("Room1"))
        val data = scheduleDataOf(emptyRoom)
        assertThat(data.roomCount).isEqualTo(1)
        assertThat(data.roomNames).isEqualTo(listOf("Room1"))
        assertThat(data.allSessions).isEqualTo(emptyList<Session>())
        assertThat(data.findSession(actualSession.guid)).isNull()
        assertThat(data.findRoomIndex(actualSession)).isEqualTo(0)
    }

    @Test
    fun `roomDataList with one room with one session queried for actual session`() {
        val roomDataList = listOf(RoomData(roomName = "Room1", sessions = listOf(actualSession)))
        val data = scheduleDataOf(roomDataList)
        val expectedSession = Session(guid = "11111111-1111-1111-1111-111111111142", roomName = "Room1")
        assertThat(data.roomCount).isEqualTo(1)
        assertThat(data.roomNames).isEqualTo(listOf("Room1"))
        assertThat(data.allSessions).isEqualTo(listOf(expectedSession))
        assertThat(data.findSession(actualSession.guid)).isEqualTo(expectedSession)
        assertThat(data.findRoomIndex(actualSession)).isEqualTo(0)
    }

    @Test
    fun `roomDataList with one room with one session queried for odd session`() {
        val roomDataList = listOf(RoomData(roomName = "Room1", sessions = listOf(actualSession)))
        val data = scheduleDataOf(roomDataList)
        assertThat(data.findSession(oddSession.guid)).isNull()
        assertThat(data.findRoomIndex(oddSession)).isEqualTo(ScheduleData.UNKNOWN_ROOM_INDEX)
    }

    @Test
    fun `roomNames returns room names sorted in display order`() {
        val roomData1 = emptyRoomOf("Room1")
        val roomData2 = emptyRoomOf("Room2")
        val roomDataList = listOf(roomData2, roomData1)
        val data = scheduleDataOf(roomDataList)
        assertThat(data.roomNames.first()).isEqualTo("Room2")
        assertThat(data.roomNames.last()).isEqualTo("Room1")
    }

    @Test
    fun `allSessions returns sessions sorted by dateUTC ascending`() {
        val session1 = Session(guid = "11111111-1111-1111-1111-111111111111", dateUTC = 200)
        val session2 = Session(guid = "11111111-1111-1111-1111-111111111112", dateUTC = 100)
        val sessions = listOf(session1, session2)
        val roomDataList = listOf(RoomData(roomName = "Room1", sessions = sessions))
        val data = scheduleDataOf(roomDataList)
        assertThat(data.allSessions.first().guid).isEqualTo("11111111-1111-1111-1111-111111111112")
        assertThat(data.allSessions.last().guid).isEqualTo("11111111-1111-1111-1111-111111111111")
    }

    private fun scheduleDataOf(roomDataList: List<RoomData>): ScheduleData {
        return ScheduleData(dayIndex = 3, roomDataList = roomDataList)
    }

    private fun emptyRoomOf(roomName: String): RoomData {
        return RoomData(roomName = roomName, sessions = emptyList())
    }

}
