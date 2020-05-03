package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

class LecturesByRoomIndexTest {

    @Test
    fun `no lectures, no rooms`() {
        val lecturesByRoom = LecturesByRoomIndex(emptyList())

        assertThat(lecturesByRoom).isEmpty()
    }

    @Test
    fun `no lectures, one rooms`() {
        val lecturesByRoom = LecturesByRoomIndex(emptyList())

        assertThat(lecturesByRoom).isEmpty()
    }

    @Test
    fun `one lecture in room zero`() {
        val roomZeroLecture = Lecture("").apply { roomIndex = 0 }
        val lecturesByRoom = LecturesByRoomIndex(listOf(roomZeroLecture))

        assertThat(lecturesByRoom).hasSize(1)
        assertThat(lecturesByRoom[0]).containsExactly(roomZeroLecture)
    }

    @Test
    fun `one lecture in room index 1 and 3 - creates extra empty rooms for index 0 and 2`() {
        val roomZeroLecture = Lecture("").apply { roomIndex = 1 }
        val roomThreeLecture = Lecture("").apply { roomIndex = 3 }
        val lecturesByRoom = LecturesByRoomIndex(listOf(roomZeroLecture, roomThreeLecture))

        assertThat(lecturesByRoom).hasSize(2)
        assertThat(lecturesByRoom[1]).containsExactly(roomZeroLecture)
        assertThat(lecturesByRoom[3]).containsExactly(roomThreeLecture)
    }

    @Test
    fun `one lecture per room - starting from zero, unordered`() {
        val roomZeroLecture = Lecture("").apply { roomIndex = 0 }
        val roomOneLecture = Lecture("").apply { roomIndex = 1 }
        val lecturesByRoom = LecturesByRoomIndex(listOf(roomOneLecture, roomZeroLecture))

        assertThat(lecturesByRoom).hasSize(2)
        assertThat(lecturesByRoom[0]).containsExactly(roomZeroLecture)
        assertThat(lecturesByRoom[1]).containsExactly(roomOneLecture)
    }

    @Test
    fun `one lecture per room - starting from one, with sparse list`() {
        val roomOneLecture = Lecture("").apply { roomIndex = 1 }
        val roomThreeLecture = Lecture("").apply { roomIndex = 3 }
        val lecturesByRoom = LecturesByRoomIndex(listOf(roomOneLecture, roomThreeLecture))

        assertThat(lecturesByRoom).hasSize(2)
        assertThat(lecturesByRoom[1]).containsExactly(roomOneLecture)
        assertThat(lecturesByRoom[3]).containsExactly(roomThreeLecture)
    }

    @Test
    fun `multiple lectures per room - starting from one, with sparse list, unordered - preserves input order`() {
        val roomOneLecture = Lecture("").apply { roomIndex = 1 }
        val roomThreeLecture1 = Lecture("31").apply { roomIndex = 3 }
        val roomThreeLecture2 = Lecture("32").apply { roomIndex = 3 }
        val roomFourLecture = Lecture("").apply { roomIndex = 4 }
        val lecturesByRoom = LecturesByRoomIndex(listOf(roomFourLecture, roomThreeLecture2, roomOneLecture, roomThreeLecture1))

        assertThat(lecturesByRoom).hasSize(3)
        assertThat(lecturesByRoom[1]).containsExactly(roomOneLecture)
        assertThat(lecturesByRoom[3]).containsExactly(roomThreeLecture1, roomThreeLecture2)
        assertThat(lecturesByRoom[3][0]).isEqualTo(roomThreeLecture2)
        assertThat(lecturesByRoom[3][1]).isEqualTo(roomThreeLecture1)
        assertThat(lecturesByRoom[4]).containsExactly(roomFourLecture)
    }
}