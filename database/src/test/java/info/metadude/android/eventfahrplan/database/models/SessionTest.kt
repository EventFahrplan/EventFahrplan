package info.metadude.android.eventfahrplan.database.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SessionTest {

    @Test
    fun `isChanged return true if all change-properties are true`() {
        val session = Session(
                sessionId = "s1",
                guid = "11111111-1111-1111-1111-111111111111",
                changedDay = true,
                changedDuration = true,
                changedLanguage = true,
                changedRecordingOptOut = true,
                changedRoom = true,
                changedSpeakers = true,
                changedSubtitle = true,
                changedTime = true,
                changedTitle = true,
                changedTrack = true
        )
        assertThat(session.isChanged).isEqualTo(true)
    }

    @Test
    fun `isChanged return false if all change-properties are false`() {
        val session = Session(
                sessionId = "s1",
                guid = "11111111-1111-1111-1111-111111111111",
                changedDay = false,
                changedDuration = false,
                changedLanguage = false,
                changedRecordingOptOut = false,
                changedRoom = false,
                changedSpeakers = false,
                changedSubtitle = false,
                changedTime = false,
                changedTitle = false,
                changedTrack = false
        )
        assertThat(session.isChanged).isEqualTo(false)
    }

}
