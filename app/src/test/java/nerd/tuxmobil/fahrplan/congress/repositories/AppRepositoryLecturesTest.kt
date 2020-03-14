package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import info.metadude.android.eventfahrplan.database.repositories.LecturesDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.dataconverters.toLecturesDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

/**
 * Test class to deal with lectures which interact with the [LecturesDatabaseRepository].
 */
class AppRepositoryLecturesTest {

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

    companion object {

        private val LECTURE_1001 = Lecture("1001").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1002 = Lecture("1002").apply {
            changedIsCanceled = true
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1003 = Lecture("1003").apply {
            changedIsCanceled = false
            changedTitle = true
            changedIsNew = false
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1004 = Lecture("1004").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

        private val LECTURE_1005 = Lecture("1005").apply {
            changedIsCanceled = true
            changedTitle = true
            changedIsNew = true
            url = "" // only initialized for toLecturesDatabaseModel()
        }

    }

    @Test
    fun `loadChangedLectures passes through an empty list`() {
        whenever(lecturesDatabaseRepository.queryLecturesOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadChangedLectures()).isEmpty()
        verify(lecturesDatabaseRepository, once()).queryLecturesOrderedByDateUtc()
    }

    @Test
    fun `loadChangedLectures filters out lectures which are not changed`() {
        val lectures = listOf(LECTURE_1001, LECTURE_1002, LECTURE_1003, LECTURE_1004, LECTURE_1005)
        whenever(lecturesDatabaseRepository.queryLecturesOrderedByDateUtc()) doReturn lectures.toLecturesDatabaseModel()
        val changedLectures = testableAppRepository.loadChangedLectures()
        assertThat(changedLectures).containsExactly(LECTURE_1002, LECTURE_1003, LECTURE_1004, LECTURE_1005)
        verify(lecturesDatabaseRepository, once()).queryLecturesOrderedByDateUtc()
    }

    private fun once() = times(1)

}
