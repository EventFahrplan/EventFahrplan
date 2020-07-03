package nerd.tuxmobil.fahrplan.congress.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ScheduleDataTest {

    private val actualLecture = Session("L42").apply { room = "Room1" }
    private val oddLecture = Session("L78").apply { room = "Room78" }

    @Test
    fun `roomDataList without rooms and lectures`() {
        val data = scheduleDataOf(emptyList())
        assertThat(data.roomCount).isEqualTo(0)
        assertThat(data.roomNames).isEqualTo(emptyList<String>())
        assertThat(data.allLectures).isEqualTo(emptyList<Session>())
        assertThat(data.findLecture(actualLecture.lectureId)).isNull()
        assertThat(data.findRoomIndex(actualLecture)).isEqualTo(ScheduleData.UNKNOWN_ROOM_INDEX)
    }

    @Test
    fun `roomDataList with one room without lectures`() {
        val emptyRoom = listOf(emptyRoomOf("Room1"))
        val data = scheduleDataOf(emptyRoom)
        assertThat(data.roomCount).isEqualTo(1)
        assertThat(data.roomNames).isEqualTo(listOf("Room1"))
        assertThat(data.allLectures).isEqualTo(emptyList<Session>())
        assertThat(data.findLecture(actualLecture.lectureId)).isNull()
        assertThat(data.findRoomIndex(actualLecture)).isEqualTo(0)
    }

    @Test
    fun `roomDataList with one room with one lecture queried for actual lecture`() {
        val roomDataList = listOf(RoomData(roomName = "Room1", lectures = listOf(actualLecture)))
        val data = scheduleDataOf(roomDataList)
        val expectedLecture = Session("L42").apply { room = "Room1" }
        assertThat(data.roomCount).isEqualTo(1)
        assertThat(data.roomNames).isEqualTo(listOf("Room1"))
        assertThat(data.allLectures).isEqualTo(listOf(expectedLecture))
        assertThat(data.findLecture(actualLecture.lectureId)).isEqualTo(expectedLecture)
        assertThat(data.findRoomIndex(actualLecture)).isEqualTo(0)
    }

    @Test
    fun `roomDataList with one room with one lecture queried for odd lecture`() {
        val roomDataList = listOf(RoomData(roomName = "Room1", lectures = listOf(actualLecture)))
        val data = scheduleDataOf(roomDataList)
        assertThat(data.findLecture(oddLecture.lectureId)).isNull()
        assertThat(data.findRoomIndex(oddLecture)).isEqualTo(ScheduleData.UNKNOWN_ROOM_INDEX)
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
    fun `allLectures returns lectures sorted by dateUTC ascending`() {
        val lecture1 = Session("L1").apply { dateUTC = 200 }
        val lecture2 = Session("L2").apply { dateUTC = 100 }
        val lectures = listOf(lecture1, lecture2)
        val roomDataList = listOf(RoomData(roomName = "Room1", lectures = lectures))
        val data = scheduleDataOf(roomDataList)
        assertThat(data.allLectures.first().lectureId).isEqualTo("L2")
        assertThat(data.allLectures.last().lectureId).isEqualTo("L1")
    }

    private fun scheduleDataOf(roomDataList: List<RoomData>): ScheduleData {
        return ScheduleData(dayIndex = 3, roomDataList = roomDataList)
    }

    private fun emptyRoomOf(roomName: String): RoomData {
        return RoomData(roomName = roomName, lectures = emptyList())
    }

}
