package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

class LectureListTransformerTest {
    private val prioritizedRoomProvider = object : PrioritizedRoomProvider {
        override val prioritizedRooms = listOf("Ada", "Borg", "Clarke", "Dijkstra", "Eliza")
    }

    private val transformer = LectureListTransformer(prioritizedRoomProvider)

    @Test
    fun `ScheduleData_dayIndex contains proper day value`() {
        val scheduleData = transformer.transformLectureList(dayIndex = 1, lectures = emptyList())

        assertThat(scheduleData.dayIndex).isEqualTo(1)
    }

    @Test
    fun `rooms are ordered by roomIndex`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Four", roomIndex = 2342),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 1),
                createLecture(lectureId = "L2", roomName = "One", roomIndex = 0),
                createLecture(lectureId = "L3", roomName = "Three", roomIndex = 4)
        )

        val scheduleData = transformer.transformLectureList(dayIndex = 0, lectures = lecturesInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("One", "Two", "Three", "Four"))
    }

    @Test
    fun `prioritized rooms are first in list`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Chaos-West Bühne", roomIndex = 11),
                createLecture(lectureId = "L1", roomName = "c-base", roomIndex = 12),
                createLecture(lectureId = "L2", roomName = "Eliza", roomIndex = 13),
                createLecture(lectureId = "L3", roomName = "Borg", roomIndex = 14),
                createLecture(lectureId = "L4", roomName = "Ada", roomIndex = 15)
        )

        val scheduleData = transformer.transformLectureList(dayIndex = 0, lectures = lecturesInDatabase)

        val roomNames = scheduleData.roomDataList.map { it.roomName }
        assertThat(roomNames).isEqualTo(listOf("Ada", "Borg", "Eliza", "Chaos-West Bühne", "c-base"))
    }

    @Test
    fun `same roomIndex does not influence RoomData contents`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Borg", roomIndex = 2),
                createLecture(lectureId = "L1", roomName = "Broken One", roomIndex = 2),
                createLecture(lectureId = "L2", roomName = "Broken Two", roomIndex = 2)
        )

        val scheduleData = transformer.transformLectureList(dayIndex = 0, lectures = lecturesInDatabase)

        with(scheduleData.roomDataList[0]) {
            assertThat(roomName).isEqualTo("Borg")
            assertThat(lectures[0].lectureId).isEqualTo("L0")
        }
        with(scheduleData.roomDataList[1]) {
            assertThat(roomName).isEqualTo("Broken One")
            assertThat(lectures[0].lectureId).isEqualTo("L1")
        }
        with(scheduleData.roomDataList[2]) {
            assertThat(roomName).isEqualTo("Broken Two")
            assertThat(lectures[0].lectureId).isEqualTo("L2")
        }
    }

    private fun createLecture(
            lectureId: String,
            roomName: String,
            roomIndex: Int,
            dateUTC: Long = 0
    ): Lecture {
        return Lecture(lectureId).apply {
            this.room = roomName
            this.roomIndex = roomIndex
            this.dateUTC = dateUTC
        }
    }
}
