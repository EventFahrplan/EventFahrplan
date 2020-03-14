package nerd.tuxmobil.fahrplan.congress.utils

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class FahrplanMiscTest {

    companion object {

        private val EVENT_1001 = Event("1001").apply {
            highlight = false
            changedIsCanceled = false
        }

        private val EVENT_1002 = Event("1002").apply {
            highlight = true
            changedIsCanceled = false
        }

        private val EVENT_1003 = Event("1003").apply {
            highlight = true
            changedIsCanceled = true
        }

        private val EVENT_1004 = Event("1004").apply {
            highlight = false
            changedIsCanceled = true
        }

        private val EVENT_3001 = Event("3001").apply {
            changedIsCanceled = false
        }

        private val EVENT_3002 = Event("3002").apply {
            changedIsCanceled = true
        }

        private val EVENT_4001 = Event("4001").apply {
            highlight = false
            changedTitle = false
        }

        private val EVENT_4002 = Event("4002").apply {
            highlight = true
            changedTitle = false
        }

        private val EVENT_4003 = Event("4003").apply {
            highlight = true
            changedTitle = true
        }

        private val EVENT_4004 = Event("4004").apply {
            highlight = false
            changedTitle = true
        }

        private val EVENT_5001 = Event("5001").apply {
            highlight = false
            changedIsNew = false
        }

        private val EVENT_5002 = Event("5002").apply {
            highlight = true
            changedIsNew = false
        }

        private val EVENT_5003 = Event("5003").apply {
            highlight = true
            changedIsNew = true
        }

        private val EVENT_5004 = Event("5004").apply {
            highlight = false
            changedIsNew = true
        }

        private val EVENT_6001 = Event("6001").apply {
            highlight = false
            changedIsCanceled = false
        }

        private val EVENT_6002 = Event("6002").apply {
            highlight = true
            changedIsCanceled = false
        }

        private val EVENT_6003 = Event("6003").apply {
            highlight = true
            changedIsCanceled = true
        }

        private val EVENT_6004 = Event("6004").apply {
            highlight = false
            changedIsCanceled = true
        }

    }

    @Test
    fun getStarredLecturesWithEmptyList() {
        val appRepository = mock<AppRepository> {
            on { loadLecturesForAllDays(anyBoolean()) } doReturn emptyList()
        }
        assertThat(FahrplanMisc.getStarredLectures(appRepository)).isEmpty()
    }

    @Test
    fun getStarredLecturesWithAllEvents() {
        val appRepository = mock<AppRepository> {
            val events = mutableListOf(EVENT_1001, EVENT_1002, EVENT_1003, EVENT_1004)
            on { loadLecturesForAllDays(anyBoolean()) } doReturn events
        }
        val starredEvents = FahrplanMisc.getStarredLectures(appRepository)
        assertThat(starredEvents).isNotEmpty()
        assertThat(starredEvents.size).isEqualTo(1)
        assertThat(starredEvents).contains(EVENT_1002)
        assertThat(starredEvents).doesNotContain(EVENT_1001, EVENT_1003, EVENT_1004)
    }

    @Test
    fun getUpcomingLecturesWithEmptyList() {
        val appRepository = mock<AppRepository> {
            on { loadLecturesForDayIndex(anyInt(), anyBoolean()) } doReturn emptyList()
        }
        assertThat(FahrplanMisc.getUncanceledLectures(appRepository, AppRepository.ALL_DAYS)).isEmpty()
    }

    @Test
    fun getUpcomingLecturesWithAllEvents() {
        val appRepository = mock<AppRepository> {
            on { loadLecturesForDayIndex(anyInt(), anyBoolean()) } doReturn mutableListOf(EVENT_3001, EVENT_3002)
        }
        val upcomingEvents = FahrplanMisc.getUncanceledLectures(appRepository, AppRepository.ALL_DAYS)
        assertThat(upcomingEvents).isNotEmpty()
        assertThat(upcomingEvents.size).isEqualTo(1)
        assertThat(upcomingEvents).contains(EVENT_3001)
        assertThat(upcomingEvents).doesNotContain(EVENT_3002)
    }

    @Test
    fun getChangedLectureCountWithEmptyListWithFavsOnly() {
        assertThat(FahrplanMisc.getChangedLectureCount(emptyList(), true)).isEqualTo(0)
    }

    @Test
    fun getChangedLectureCountWithEmptyList() {
        assertThat(FahrplanMisc.getChangedLectureCount(emptyList(), false)).isEqualTo(0)
    }

    @Test
    fun getChangedLectureCountWithAllEventsWithFavsOnly() {
        val events = listOf(EVENT_4001, EVENT_4002, EVENT_4003, EVENT_4004)
        assertThat(FahrplanMisc.getChangedLectureCount(events, true)).isEqualTo(1)
    }

    @Test
    fun getChangedLectureCountWithAllEvents() {
        val events = listOf(EVENT_4001, EVENT_4002, EVENT_4003, EVENT_4004)
        assertThat(FahrplanMisc.getChangedLectureCount(events, false)).isEqualTo(2)
    }

    @Test
    fun getNewLectureCountWithEmptyListWithFavsOnly() {
        assertThat(FahrplanMisc.getNewLectureCount(emptyList(), true)).isEqualTo(0)
    }

    @Test
    fun getNewLectureCountWithEmptyList() {
        assertThat(FahrplanMisc.getNewLectureCount(emptyList(), false)).isEqualTo(0)
    }

    @Test
    fun getNewLectureCountWithAllEventsWithFavsOnly() {
        val events = listOf(EVENT_5001, EVENT_5002, EVENT_5003, EVENT_5004)
        assertThat(FahrplanMisc.getNewLectureCount(events, true)).isEqualTo(1)
    }

    @Test
    fun getNewLectureCountWithAllEvents() {
        val events = listOf(EVENT_5001, EVENT_5002, EVENT_5003, EVENT_5004)
        assertThat(FahrplanMisc.getNewLectureCount(events, false)).isEqualTo(2)
    }

    @Test
    fun getCancelledLectureCountWithEmptyListWithFavsOnly() {
        assertThat(FahrplanMisc.getCancelledLectureCount(emptyList(), true)).isEqualTo(0)
    }

    @Test
    fun getCancelledLectureCountWithEmptyList() {
        assertThat(FahrplanMisc.getCancelledLectureCount(emptyList(), false)).isEqualTo(0)
    }

    @Test
    fun getCancelledLectureCountWithAllEventsWithFavsOnly() {
        val events = listOf(EVENT_6001, EVENT_6002, EVENT_6003, EVENT_6004)
        assertThat(FahrplanMisc.getCancelledLectureCount(events, true)).isEqualTo(1)
    }

    @Test
    fun getCancelledLectureCountWithAllEvents() {
        val events = listOf(EVENT_6001, EVENT_6002, EVENT_6003, EVENT_6004)
        assertThat(FahrplanMisc.getCancelledLectureCount(events, false)).isEqualTo(2)
    }

}
