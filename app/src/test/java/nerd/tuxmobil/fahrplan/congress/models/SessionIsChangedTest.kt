package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Covers [Session.isChanged].
 */
class SessionIsChangedTest {

    @Test
    fun `a default session is not marked as changed`() {
        val session = createSession()
        assertThat(session.isChanged).isFalse()
    }

    @Test
    fun `changedTitle = true marks a session as changed`() {
        val session = createSession().copy(changedTitle = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedSubtitle = true marks a session as changed`() {
        val session = createSession().copy(changedSubtitle = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedRoomName = true marks a session as changed`() {
        val session = createSession().copy(changedRoomName = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedDayIndex = true marks a session as changed`() {
        val session = createSession().copy(changedDayIndex = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedStartTime = true marks a session as changed`() {
        val session = createSession().copy(changedStartTime = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedDuration = true marks a session as changed`() {
        val session = createSession().copy(changedDuration = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedSpeakers = true marks a session as changed`() {
        val session = createSession().copy(changedSpeakers = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedLanguage = true marks a session as changed`() {
        val session = createSession().copy(changedLanguage = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedRecordingOptOut = true marks a session as changed`() {
        val session = createSession().copy(changedRecordingOptOut = true)
        assertThat(session.isChanged).isTrue()
    }

    @Test
    fun `changedTrack = true marks a session as changed`() {
        val session = createSession().copy(changedTrack = true)
        assertThat(session.isChanged).isTrue()
    }

    private fun createSession() = Session(
        guid = "",
        changedTitle = false,
        changedSubtitle = false,
        changedRoomName = false,
        changedDayIndex = false,
        changedStartTime = false,
        changedDuration = false,
        changedSpeakers = false,
        changedLanguage = false,
        changedRecordingOptOut = false,
        changedTrack = false,
    )

}
