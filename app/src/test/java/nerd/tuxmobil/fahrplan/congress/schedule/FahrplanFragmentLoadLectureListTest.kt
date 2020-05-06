package nerd.tuxmobil.fahrplan.congress.schedule

import android.support.v4.util.SparseArrayCompat
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import info.metadude.android.eventfahrplan.database.repositories.LecturesDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Lecture as DatabaseLecture

class FahrplanFragmentLoadLectureListTest {

    private val lecturesDatabaseRepository = mock<LecturesDatabaseRepository>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                    context = mock(),
                    logging = mock(),
                    networkScope = mock(),
                    alarmsDatabaseRepository = mock(),
                    highlightsDatabaseRepository = mock(),
                    lecturesDatabaseRepository = lecturesDatabaseRepository,
                    metaDatabaseRepository = mock(),
                    scheduleNetworkRepository = mock(),
                    engelsystemNetworkRepository = mock(),
                    sharedPreferencesRepository = mock()
            )
            return this
        }

    private val onDoneDoNothing = {}

    @Test
    fun `Skips computation when "appRepository" returns an empty "lectureList" but still updates "lectureList"`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap.clear()
        MyApp.roomList.clear()
        val dayIndex = 0
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn emptyList()

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        assertThat(MyApp.lectureList as List<*>).isEqualTo(emptyList<DatabaseLecture>()) // Is updated.
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(emptyMap<String, Int>())
        assertThat(MyApp.roomList.toString()).isEqualTo(SparseArrayCompat<Int>().toString())
        assertThat(MyApp.roomCount).isEqualTo(0)
    }

    @Test
    fun `Records a lecture in "lectureList", "roomsMap" and increments "roomCount"`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lectures = listOf(DatabaseLecture(eventId = "L0", room = "Ada", roomIndex = 0))
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(createLecture("L0", "Ada"))
        val expectedRoomList = SparseArrayCompat<Int>().apply { append(0, 0) }
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Ada" to 0))
        assertThat(MyApp.roomList.toString()).isEqualTo(expectedRoomList.toString())
        assertThat(MyApp.roomCount).isEqualTo(1)
    }

    @Test
    fun `Records a lecture with its predefined "roomIndex" not being considered for "roomList" nor "roomsMap"`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lectures = listOf(DatabaseLecture(eventId = "L0", room = "Ada", roomIndex = 17))
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(createLecture("L0", "Ada"))
        val expectedRoomList = SparseArrayCompat<Int>().apply { append(0, 0) }
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Ada" to 0))
        assertThat(MyApp.roomList.toString()).isEqualTo(expectedRoomList.toString())
        assertThat(MyApp.roomCount).isEqualTo(1)
    }

    @Test
    fun `Synchronizes the "roomIndex" of lectures in the same "room"`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lecture0 = DatabaseLecture(eventId = "L0", room = "Ada", roomIndex = 17)
        val lecture1 = DatabaseLecture(eventId = "L1", room = "Ada", roomIndex = 0)
        val lectures = listOf(lecture0, lecture1)
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(
                createLecture("L0", "Ada"),
                createLecture("L1", "Ada"))
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Ada" to 0))
        assertThat(MyApp.roomList.toPairList()).isEqualTo(listOf(0 to 0))
        assertThat(MyApp.roomCount).isEqualTo(1)
    }

    @Test
    fun `Applies a new "roomIndex" to all lectures which have no consecutive order`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lecture0 = DatabaseLecture(eventId = "L0", room = "Turing", roomIndex = 17)
        val lecture1 = DatabaseLecture(eventId = "L1", room = "Lovelace", roomIndex = 17)
        val lectures = listOf(lecture0, lecture1)
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(
                createLecture("L0", "Turing"),
                createLecture("L1", "Lovelace"))
        val expectedRoomList = SparseArrayCompat<Int>().apply {
            append(0, 0)
            append(1, 1)
        }
        assertThat(lecture1.roomIndex).isEqualTo(17) // Stays unmodified because it has not relevance for UI code.
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Turing" to 0, "Lovelace" to 1))
        assertThat(MyApp.roomList.toString()).isEqualTo(expectedRoomList.toString())
        assertThat(MyApp.roomCount).isEqualTo(2)
    }

    @Test
    fun `Applies a new "roomIndex" to a lecture which takes place in a prioritized room and prepends the room in the "roomList"`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lecture0 = DatabaseLecture(eventId = "L0", room = "Ada", roomIndex = 13)
        val lecture1 = DatabaseLecture(eventId = "L1", room = "Saal G", roomIndex = 13)
        val lectures = listOf(lecture0, lecture1)
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(
                createLecture("L0", "Ada"),
                createLecture("L1", "Saal G"))
        val expectedRoomList = SparseArrayCompat<Int>().apply {
            append(0, 0) // The "Saal G" room is moved to the front.
            append(1, 1) // The "Ada" room is moved to the back.
        }
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Ada" to 1, "Saal G" to 0))
        assertThat(MyApp.roomList.toString()).isEqualTo(expectedRoomList.toString())
        assertThat(MyApp.roomCount).isEqualTo(2)
    }

    @Test
    fun `Applies a new "roomIndex" to a lecture which does not take place in prioritized room`() {
        MyApp.lectureList = null
        MyApp.lectureListDay = 0
        MyApp.roomCount = 0
        MyApp.roomsMap["Borg"] = 42
        MyApp.roomList.append(0, 42)
        val dayIndex = 3
        val lecture0 = DatabaseLecture(eventId = "L0", room = "Manning", roomIndex = 0)
        val lecture1 = DatabaseLecture(eventId = "L1", room = "Snowden", roomIndex = 0)
        val lectures = listOf(lecture0, lecture1)
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(
                createLecture("L0", "Manning"),
                createLecture("L1", "Snowden"))
        val expectedRoomList = SparseArrayCompat<Int>().apply {
            append(0, 0)
            append(1, 1)
        }
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
        assertThat(MyApp.lectureListDay).isEqualTo(dayIndex)
        assertThat(MyApp.roomsMap).isEqualTo(mapOf("Manning" to 0, "Snowden" to 1))
        assertThat(MyApp.roomList.toString()).isEqualTo(expectedRoomList.toString())
        assertThat(MyApp.roomCount).isEqualTo(2)
    }

    @Test
    fun `Sorts "lectureList" by "dateUTC" field`() {
        MyApp.lectureList = null
        val dayIndex = 0
        val lecture0 = DatabaseLecture(eventId = "L0", room = "Ada", roomIndex = 0, dateUTC = 200)
        val lecture1 = DatabaseLecture(eventId = "L1", room = "Ada", roomIndex = 0, dateUTC = 100)
        val lectures = listOf(lecture0, lecture1)
        whenever(lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex)) doReturn lectures

        FahrplanFragment.loadLectureList(NoLogging, testableAppRepository, dayIndex, false, onDoneDoNothing)

        val expectedLectures = listOf(
                createLecture("L1", "Ada", 100),
                createLecture("L0", "Ada", 200))
        assertThat(MyApp.lectureList as List<*>).isEqualTo(expectedLectures)
    }

    private fun createLecture(lectureId: String, roomName: String, dateUTC: Long = 0L) =
            Lecture(lectureId).apply {
                this.room = roomName
                this.roomIndex = -1 // Not part of Lecture#equals, therefore ignored.
                this.dateUTC = dateUTC
            }

    private fun <T> SparseArrayCompat<T>.toPairList() =
            List(size()) { index -> keyAt(index) to valueAt(index) }

}
