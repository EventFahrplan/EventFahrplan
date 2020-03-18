package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class FahrplanMiscTest {

    companion object {

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
