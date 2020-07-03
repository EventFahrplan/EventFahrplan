package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.dataconverters.toLecturesDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

/**
 * Test class to deal with lectures which interact with the [SessionsDatabaseRepository].
 */
class AppRepositorySessionsTest {

    private val lecturesDatabaseRepository = mock<SessionsDatabaseRepository>()

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

    companion object {

        private val LECTURE_1001 = Session("1001").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1002 = Session("1002").apply {
            changedIsCanceled = true
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1003 = Session("1003").apply {
            changedIsCanceled = false
            changedTitle = true
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1004 = Session("1004").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1005 = Session("1005").apply {
            changedIsCanceled = true
            changedTitle = true
            changedIsNew = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_2001 = Session("2001").apply {
            highlight = false
            changedIsCanceled = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_2002 = Session("2002").apply {
            highlight = true
            changedIsCanceled = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_2003 = Session("2003").apply {
            highlight = true
            changedIsCanceled = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_2004 = Session("2004").apply {
            highlight = false
            changedIsCanceled = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_3001 = Session("3001").apply {
            changedIsCanceled = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_3002 = Session("3002").apply {
            changedIsCanceled = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

    }

    @Test
    fun `loadChangedLectures passes through an empty list`() {
        whenever(lecturesDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadChangedLectures()).isEmpty()
        verify(lecturesDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadChangedLectures filters out lectures which are not changed`() {
        val lectures = listOf(LECTURE_1001, LECTURE_1002, LECTURE_1003, LECTURE_1004, LECTURE_1005)
        whenever(lecturesDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn lectures.toLecturesDatabaseModel()
        val changedLectures = testableAppRepository.loadChangedLectures()
        assertThat(changedLectures).containsExactly(LECTURE_1002, LECTURE_1003, LECTURE_1004, LECTURE_1005)
        verify(lecturesDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredLectures passes through an empty list`() {
        whenever(lecturesDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadStarredLectures()).isEmpty()
        verify(lecturesDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredLectures filters out lectures which are not starred`() {
        val lectures = listOf(LECTURE_2001, LECTURE_2002, LECTURE_2003, LECTURE_2004)
        whenever(lecturesDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn lectures.toLecturesDatabaseModel()
        val starredLectures = testableAppRepository.loadStarredLectures()
        assertThat(starredLectures).containsExactly(LECTURE_2002)
        verify(lecturesDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadUncanceledLecturesForDayIndex passes through an empty list`() {
        whenever(lecturesDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn emptyList()
        assertThat(testableAppRepository.loadUncanceledLecturesForDayIndex(0)).isEmpty()
        verify(lecturesDatabaseRepository, once()).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    @Test
    fun `loadUncanceledLecturesForDayIndex filters out lectures which are canceled`() {
        val lectures = listOf(LECTURE_3001, LECTURE_3002)
        whenever(lecturesDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn lectures.toLecturesDatabaseModel()
        val uncanceledLectures = testableAppRepository.loadUncanceledLecturesForDayIndex(0)
        assertThat(uncanceledLectures).containsExactly(LECTURE_3001)
        verify(lecturesDatabaseRepository, once()).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    private fun once() = times(1)

}
