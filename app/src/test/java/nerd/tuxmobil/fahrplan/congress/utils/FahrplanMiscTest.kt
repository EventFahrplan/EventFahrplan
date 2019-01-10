package nerd.tuxmobil.fahrplan.congress.utils

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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

        private val EVENT_2001 = Event("2001").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = false
        }

        private val EVENT_2002 = Event("2002").apply {
            changedIsCanceled = true
            changedTitle = false
            changedIsNew = false
        }

        private val EVENT_2003 = Event("2003").apply {
            changedIsCanceled = false
            changedTitle = true
            changedIsNew = false
        }

        private val EVENT_2004 = Event("2004").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = true
        }

        private val EVENT_2005 = Event("2005").apply {
            changedIsCanceled = true
            changedTitle = true
            changedIsNew = true
        }

        private val EVENT_3001 = Event("3001").apply {
            changedIsCanceled = false
        }

        private val EVENT_3002 = Event("3002").apply {
            changedIsCanceled = true
        }

    }

    @Test
    fun getStarredLecturesWithEmptyList() {
        val appRepository = mock<AppRepository> {
            on { readLecturesOrderedByDateUtc() } doReturn emptyList()
        }
        assertThat(FahrplanMisc.getStarredLectures(appRepository)).isEmpty()
    }

    @Test
    fun getStarredLecturesWithAllEvents() {
        val appRepository = mock<AppRepository> {
            val events = mutableListOf(EVENT_1001, EVENT_1002, EVENT_1003, EVENT_1004)
            on { readLecturesOrderedByDateUtc() } doReturn events
        }
        val starredEvents = FahrplanMisc.getStarredLectures(appRepository)
        assertThat(starredEvents).isNotEmpty()
        assertThat(starredEvents.size).isEqualTo(1)
        assertThat(starredEvents).contains(EVENT_1002)
        assertThat(starredEvents).doesNotContain(EVENT_1001, EVENT_1003, EVENT_1004)
    }

    @Test
    fun readChangesWithEmptyList() {
        val appRepository = mock<AppRepository> {
            on { readLecturesOrderedByDateUtc() } doReturn emptyList()
        }
        assertThat(FahrplanMisc.readChanges(appRepository)).isEmpty()
    }

    @Test
    fun readChangesWithAllEvents() {
        val appRepository = mock<AppRepository> {
            val events = mutableListOf(EVENT_2001, EVENT_2002, EVENT_2003, EVENT_2004, EVENT_2005)
            on { readLecturesOrderedByDateUtc() } doReturn events
        }
        val changedEvents = FahrplanMisc.readChanges(appRepository)
        assertThat(changedEvents).isNotEmpty()
        assertThat(changedEvents.size).isEqualTo(4)
        assertThat(changedEvents).contains(EVENT_2002, EVENT_2003, EVENT_2004, EVENT_2005)
        assertThat(changedEvents).doesNotContain(EVENT_2001)
    }

    @Test
    fun getUpcomingLecturesWithEmptyList() {
        val appRepository = mock<AppRepository> {
            on { readLecturesOrderedByDateUtc() } doReturn emptyList()
        }
        assertThat(FahrplanMisc.getUncanceledLectures(appRepository, FahrplanMisc.ALL_DAYS)).isEmpty()
    }

    @Test
    fun getUpcomingLecturesWithAllEvents() {
        val appRepository = mock<AppRepository> {
            on { readLecturesOrderedByDateUtc() } doReturn mutableListOf(EVENT_3001, EVENT_3002)
        }
        val upcomingEvents = FahrplanMisc.getUncanceledLectures(appRepository, FahrplanMisc.ALL_DAYS)
        assertThat(upcomingEvents).isNotEmpty()
        assertThat(upcomingEvents.size).isEqualTo(1)
        assertThat(upcomingEvents).contains(EVENT_3001)
        assertThat(upcomingEvents).doesNotContain(EVENT_3002)
    }

}
