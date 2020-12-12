package nerd.tuxmobil.fahrplan.congress.serialization

import nerd.tuxmobil.fahrplan.congress.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.ArrayList

class ScheduleChangesTest {

    private val oldSessions = ArrayList<Session>()
    private val newSessions = ArrayList<Session>()

    @Test
    fun hasScheduleChangedWithEmptyOldSessions() {
        assertThat(ScheduleChanges.hasScheduleChanged(newSessions, mutableListOf())).isFalse()
    }

    @Test
    fun hasScheduleChangedWithSameId() {
        oldSessions.add(Session("sessionId3").apply {
            this.url = "" // TODO Remove when replacing hasScheduleChanged.
        })
        newSessions.add(Session("sessionId3").apply {
            this.url = "" // TODO Remove when replacing hasScheduleChanged.
        })
        assertThat(ScheduleChanges.hasScheduleChanged(newSessions, oldSessions)).isFalse()
    }

    @Test
    fun hasScheduleChangedWithNewId() {
        oldSessions.add(Session("sessionId3").apply {
            this.url = "" // TODO Remove when replacing hasScheduleChanged.
        })
        newSessions.add(Session("sessionId7").apply {
            this.url = "" // TODO Remove when replacing hasScheduleChanged.
        })
        assertThat(ScheduleChanges.hasScheduleChanged(newSessions, oldSessions)).isTrue()
    }

    @Test
    fun hasScheduleChangedWithChangedIsCanceled() = assertSessionHasChanged { oldSession, _ ->
        oldSession.changedIsCanceled = true
    }

    @Test
    fun hasScheduleChangedWithOddTitles() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.title = "Old title"
        newSession.title = "New title"
    }

    @Test
    fun hasScheduleChangedWithOddSubtitles() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.subtitle = "Old subtitle"
        newSession.subtitle = "New subtitle"
    }

    @Test
    fun hasScheduleChangedWithOddSpeakers() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.speakers = "Old speakers"
        newSession.speakers = "New speakers"
    }

    @Test
    fun hasScheduleChangedWithOddLanguages() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.lang = "de"
        newSession.lang = "en"
    }

    @Test
    fun hasScheduleChangedWithOddRooms() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.room = "Room 1"
        newSession.room = "Room A"
    }

    @Test
    fun hasScheduleChangedWithOddTracks() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.track = "hardware"
        newSession.track = "software"
    }

    @Test
    fun hasScheduleChangedWithOddRecordingOptOut() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.recordingOptOut = true
        newSession.recordingOptOut = false
    }

    @Test
    fun hasScheduleChangedWithOddDays() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.day = 4
        newSession.day = 1
    }

    @Test
    fun hasScheduleChangedWithOddStartTimes() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.startTime = 1185
        newSession.startTime = 1410
    }

    @Test
    fun hasScheduleChangedWithOddDurations() = assertSessionHasChanged { oldSession, newSession ->
        oldSession.duration = 30
        newSession.duration = 60
    }

    private fun assertSessionHasChanged(modify: (oldSession: Session, newSession: Session) -> Unit) {
        val oldSession = Session("sessionId3").apply {
            url = "" // TODO Remove when replacing hasScheduleChanged.
        }
        val newSession = Session("sessionId3").apply {
            url = "" // TODO Remove when replacing hasScheduleChanged.
        }
        modify.invoke(oldSession, newSession)
        oldSessions.add(oldSession)
        newSessions.add(newSession)
        assertThat(ScheduleChanges.hasScheduleChanged(newSessions, oldSessions)).isTrue()
    }

}
