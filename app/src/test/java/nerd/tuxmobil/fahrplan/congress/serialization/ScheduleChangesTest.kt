package nerd.tuxmobil.fahrplan.congress.serialization

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.network.models.Session
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.toFlaggedSessions
import org.junit.Test

class ScheduleChangesTest {

    @Test
    fun `toFlaggedSessions returns new sessions and does not invoke onChangesFound if there are no sessions`() {
        val oldSessions = listOf<Session>()
        val newSessions = listOf<Session>()
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(newSessions)
        assertThat(onChangesFound).isFalse()
    }

    @Test
    fun `toFlaggedSessions returns new sessions and does not invoke onChangesFound if old sessions are canceled`() {
        val oldSessions = listOf(Session().apply { changedIsCanceled = true })
        val newSessions = listOf<Session>()
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(newSessions)
        assertThat(onChangesFound).isFalse()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if new session is present`() {
        fun createCanceledSessions(sessionId: String) = listOf(Session().apply {
            this.sessionId = sessionId
            this.changedIsCanceled = true
        })

        fun createNewSessions(sessionId: String) = listOf(Session().apply {
            this.sessionId = sessionId
            this.changedIsNew = false
        })

        val oldSessions = createCanceledSessions("canceled")
        val newSessions = createNewSessions("new")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "new"
            changedIsNew = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions returns new sessions and does not invoke onChangesFound if there are no changes`() {
        fun createSessions() = listOf(Session().apply {
            sessionId = "s1"
            title = "title"
            subtitle = "subtitle"
            speakers = "speakers"
            language = "language"
            room = "room"
            recordingOptOut = true
            dayIndex = 3
            startTime = 200
            duration = 90
        })

        val oldSessions = createSessions()
        val newSessions = createSessions()
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(createSessions())
        assertThat(onChangesFound).isFalse()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if title has changed`() {
        fun createSessions(title: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.title = title
        })

        val oldSessions = createSessions("Old title")
        val newSessions = createSessions("New title")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            title = "New title"
            changedTitle = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if subtitle has changed`() {
        fun createSessions(subtitle: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.subtitle = subtitle
        })

        val oldSessions = createSessions("Old subtitle")
        val newSessions = createSessions("New subtitle")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            subtitle = "New subtitle"
            changedSubtitle = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if speakers has changed`() {
        fun createSessions(speakers: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.speakers = speakers
        })

        val oldSessions = createSessions("Old speakers")
        val newSessions = createSessions("New speakers")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            speakers = "New speakers"
            changedSpeakers = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if language has changed`() {
        fun createSessions(language: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.language = language
        })

        val oldSessions = createSessions("Old language")
        val newSessions = createSessions("New language")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            language = "New language"
            changedLanguage = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if room has changed`() {
        fun createSessions(room: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.room = room
        })

        val oldSessions = createSessions("Old room")
        val newSessions = createSessions("New room")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            room = "New room"
            changedRoom = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if track has changed`() {
        fun createSessions(track: String) = listOf(Session().apply {
            this.sessionId = "s1"
            this.track = track
        })

        val oldSessions = createSessions("Old track")
        val newSessions = createSessions("New track")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            track = "New track"
            changedTrack = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if recordingOptOut has changed`() {
        fun createSessions(recordingOptOut: Boolean) = listOf(Session().apply {
            this.sessionId = "s1"
            this.recordingOptOut = recordingOptOut
        })

        val oldSessions = createSessions(false)
        val newSessions = createSessions(true)
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            recordingOptOut = true
            changedRecordingOptOut = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if dayIndex has changed`() {
        fun createSessions(dayIndex: Int) = listOf(Session().apply {
            this.sessionId = "s1"
            this.dayIndex = dayIndex
        })

        val oldSessions = createSessions(1)
        val newSessions = createSessions(2)
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            dayIndex = 2
            changedDayIndex = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if startTime has changed`() {
        fun createSessions(startTime: Int) = listOf(Session().apply {
            this.sessionId = "s1"
            this.startTime = startTime
        })

        val oldSessions = createSessions(100)
        val newSessions = createSessions(200)
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            startTime = 200
            changedStartTime = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags and returns new sessions and invokes onChangesFound if duration has changed`() {
        fun createSessions(duration: Int) = listOf(Session().apply {
            this.sessionId = "s1"
            this.duration = duration
        })

        val oldSessions = createSessions(45)
        val newSessions = createSessions(60)
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(Session().apply {
            sessionId = "s1"
            duration = 60
            changedDuration = true
        }))
        assertThat(onChangesFound).isTrue()
    }

    @Test
    fun `toFlaggedSessions flags new and canceled sessions, returns them and invokes onChangesFound`() {
        fun createSessions(sessionId: String) = listOf(Session().apply {
            this.sessionId = sessionId
        })

        val oldSessions = createSessions("s1")
        val newSessions = createSessions("s2")
        val (sessions, onChangesFound) = newSessions.toFlaggedSessions(oldSessions)
        assertThat(sessions).isEqualTo(listOf(
                Session().apply {
                    sessionId = "s2"
                    changedIsNew = true
                },
                Session().apply {
                    sessionId = "s1"
                    changedIsCanceled = true
                }
        ))
        assertThat(onChangesFound).isTrue()
    }

    private fun List<Session>.toFlaggedSessions(oldSessions: List<Session>): Pair<List<Session>, Boolean> {
        var onChangesFound = false
        val sessions = toFlaggedSessions(oldSessions) {
            onChangesFound = true
        }
        return sessions to onChangesFound
    }

}
