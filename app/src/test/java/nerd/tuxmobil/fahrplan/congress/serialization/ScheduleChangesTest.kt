package nerd.tuxmobil.fahrplan.congress.serialization

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.Companion.computeSessionsWithChangeFlags
import org.junit.Test

class ScheduleChangesTest {

    @Test
    fun `computeSessionsWithChangeFlags returns new sessions and false if there are no sessions`() {
        val oldSessions = emptyList<Session>()
        val newSessions = emptyList<Session>()
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(newSessions)
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isFalse()
    }

    @Test
    fun `computeSessionsWithChangeFlags returns new sessions, canceled sessions and false if old sessions are canceled`() {
        val oldSessions = listOf(createSession { changedIsCanceled = true })
        val newSessions = emptyList<Session>()
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(newSessions)
        assertThat(scheduleChanges.oldCanceledSessions).isEqualTo(listOf(createSession { changedIsCanceled = true }))
        assertThat(scheduleChanges.foundChanges).isFalse()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions, canceled sessions and true if new session is present`() {
        val oldSessions = listOf(createSession(sessionId = "canceled") { changedIsCanceled = true })
        val newSessions = listOf(createSession(sessionId = "new") { changedIsCanceled = false })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession(sessionId = "new") { changedIsNew = true }))
        assertThat(scheduleChanges.oldCanceledSessions).isEqualTo(listOf(createSession(sessionId = "canceled") { changedIsCanceled = true }))
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags returns new sessions and false if there are no changes`() {
        fun createSessions() = listOf(createSession {
            title = "title"
            subtitle = "subtitle"
            speakers = listOf("speakers")
            lang = "language"
            room = "room"
            recordingOptOut = true
            day = 3
            startTime = 200
            duration = 90
        })

        val oldSessions = createSessions()
        val newSessions = createSessions()
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(createSessions())
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isFalse()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if title has changed`() {
        val oldSessions = listOf(createSession { title = "Old title" })
        val newSessions = listOf(createSession { title = "New title" })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            title = "New title"
            changedTitle = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if subtitle has changed`() {
        val oldSessions = listOf(createSession { subtitle = "Old subtitle" })
        val newSessions = listOf(createSession { subtitle = "New subtitle" })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            subtitle = "New subtitle"
            changedSubtitle = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if speakers has changed`() {
        val oldSessions = listOf(createSession { speakers = listOf("Old speakers") })
        val newSessions = listOf(createSession { speakers = listOf("New speakers") })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            speakers = listOf("New speakers")
            changedSpeakers = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if language has changed`() {
        val oldSessions = listOf(createSession { lang = "Old language" })
        val newSessions = listOf(createSession { lang = "New language" })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            lang = "New language"
            changedLanguage = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if room has changed`() {
        val oldSessions = listOf(createSession { room = "Old room" })
        val newSessions = listOf(createSession { room = "New room" })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            room = "New room"
            changedRoom = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if track has changed`() {
        val oldSessions = listOf(createSession { track = "Old track" })
        val newSessions = listOf(createSession { track = "New track" })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            track = "New track"
            changedTrack = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if recordingOptOut has changed`() {
        val oldSessions = listOf(createSession { recordingOptOut = false })
        val newSessions = listOf(createSession { recordingOptOut = true })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            recordingOptOut = true
            changedRecordingOptOut = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if dayIndex has changed`() {
        val oldSessions = listOf(createSession { day = 1 })
        val newSessions = listOf(createSession { day = 2 })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            day = 2
            changedDay = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if startTime has changed`() {
        val oldSessions = listOf(createSession { startTime = 100 })
        val newSessions = listOf(createSession { startTime = 200 })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            startTime = 200
            changedTime = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if duration has changed`() {
        val oldSessions = listOf(createSession { duration = 45 })
        val newSessions = listOf(createSession { duration = 60 })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            duration = 60
            changedDuration = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags new and canceled sessions, returns them and true`() {
        val oldSessions = listOf(createSession(sessionId = "s1"))
        val newSessions = listOf(createSession(sessionId = "s2"))
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(
                createSession(sessionId = "s2") { changedIsNew = true },
                createSession(sessionId = "s1") { changedIsCanceled = true }
        ))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    @Test
    fun `computeSessionsWithChangeFlags flags and returns new sessions and true if multiple properties have changed`() {
        val oldSessions = listOf(createSession {
            title = "Old title"
            subtitle = "Old subtitle"
            speakers = listOf("Old speakers")
            lang = "Old language"
            room = "Old room"
            day = 2
            track = "Old track"
            recordingOptOut = false
            startTime = 200
            duration = 30
            changedTitle = false
            changedSubtitle = false
            changedSpeakers = false
            changedLanguage = false
            changedRoom = false
            changedDay = false
            changedTrack = false
            changedRecordingOptOut = false
            changedTime = false
            changedDuration = false

        })
        val newSessions = listOf(createSession {
            title = "New title"
            subtitle = "New subtitle"
            speakers = listOf("New speakers")
            lang = "New language"
            room = "New room"
            day = 3
            track = "New track"
            recordingOptOut = true
            startTime = 300
            duration = 45
            changedTitle = false
            changedSubtitle = false
            changedSpeakers = false
            changedLanguage = false
            changedRoom = false
            changedDay = false
            changedTrack = false
            changedRecordingOptOut = false
            changedTime = false
            changedDuration = false
        })
        val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
        assertThat(scheduleChanges.sessionsWithChangeFlags).isEqualTo(listOf(createSession {
            title = "New title"
            subtitle = "New subtitle"
            speakers = listOf("New speakers")
            lang = "New language"
            room = "New room"
            day = 3
            track = "New track"
            recordingOptOut = true
            startTime = 300
            duration = 45
            changedTitle = true
            changedSubtitle = true
            changedSpeakers = true
            changedLanguage = true
            changedRoom = true
            changedDay = true
            changedTrack = true
            changedRecordingOptOut = true
            changedTime = true
            changedDuration = true
        }))
        assertThat(scheduleChanges.oldCanceledSessions).isEmpty()
        assertThat(scheduleChanges.foundChanges).isTrue()
    }

    private fun createSession(sessionId: String = "1", block: Session.() -> Unit = {}): Session {
        return Session(sessionId).apply {
            guid = sessionId // shortcut for testing purpose only!
            block()
        }
    }

}
