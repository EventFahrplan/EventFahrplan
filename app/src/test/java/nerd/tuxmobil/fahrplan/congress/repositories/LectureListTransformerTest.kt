package nerd.tuxmobil.fahrplan.congress.repositories

import android.support.v4.util.SparseArrayCompat
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

    @Test
    fun `lectureListDay contains proper dayIndex value`() {
        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = emptyList())

        assertThat(result.lectureListDay).isEqualTo(1)
    }

    @Test
    fun `lectureList contains all lectures`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Ada", roomIndex = 0),
                createLecture(lectureId = "L1", roomName = "Borg", roomIndex = 1),
                createLecture(lectureId = "L2", roomName = "Clarke", roomIndex = 2)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        val lectureIds = result.lectureList.map { it.lectureId }.toSet()
        assertThat(lectureIds).isEqualTo(setOf("L0", "L1", "L2"))
    }

    @Test
    fun `lectureList is sorted by dateUTC`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Ada", roomIndex = 0, dateUTC = 300),
                createLecture(lectureId = "L1", roomName = "Borg", roomIndex = 1, dateUTC = 100),
                createLecture(lectureId = "L2", roomName = "Clarke", roomIndex = 2, dateUTC = 200)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        val lectureIds = result.lectureList.map { it.lectureId }
        assertThat(lectureIds).isEqualTo(listOf("L1", "L2", "L0"))
    }

    @Test
    fun `roomsMap only contains rooms with lectures`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "One", roomIndex = 10),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 11),
                createLecture(lectureId = "L2", roomName = "Three", roomIndex = 12)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        val roomNames = result.roomsMap.keys.toSet()
        assertThat(roomNames).isEqualTo(setOf("One", "Two", "Three"))
    }

    @Test
    fun `roomsMap is using rewritten roomIndex`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Three", roomIndex = 12),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 11),
                createLecture(lectureId = "L2", roomName = "One", roomIndex = 10)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        assertThat(result.roomsMap).isEqualTo(mapOf("One" to 0, "Two" to 1, "Three" to 2))
    }

    @Test
    fun `roomList maps to correct room index`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Three", roomIndex = 12),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 11),
                createLecture(lectureId = "L2", roomName = "One", roomIndex = 10)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        assertThat(result.roomList.toMap()).isEqualTo(mapOf(0 to 0, 1 to 1, 2 to 2))
    }

    @Test
    fun `Lecture_roomIndex is updated with rewritten roomIndex`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "Three", roomIndex = 12),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 11),
                createLecture(lectureId = "L2", roomName = "One", roomIndex = 10)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        assertThat(result.lectureList.first { it.room == "One" }.roomIndex).isEqualTo(0)
        assertThat(result.lectureList.first { it.room == "Two" }.roomIndex).isEqualTo(1)
        assertThat(result.lectureList.first { it.room == "Three" }.roomIndex).isEqualTo(2)
    }

    @Test
    fun `roomCount matches number of rooms`() {
        val lecturesInDatabase = listOf(
                createLecture(lectureId = "L0", roomName = "One", roomIndex = 10),
                createLecture(lectureId = "L1", roomName = "Two", roomIndex = 11)
        )

        val result = transformer.legacyTransformLectureList(dayIndex = 1, lectures = lecturesInDatabase)

        assertThat(result.roomCount).isEqualTo(2)
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

    private fun SparseArrayCompat<Int>.toMap() =
            (0 until size()).map { index ->
                keyAt(index) to valueAt(index)
            }.toMap()
}
