package info.metadude.android.eventfahrplan.network.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SessionTest {

    @Test
    fun `cancel marks a session as canceled and resets all change other flags`() {
        val session = Session(
            guid = "11111111-1111-1111-1111-111111111111",
            changedTitle = true,
            changedSubtitle = true,
            changedRoomName = true,
            changedDayIndex = true,
            changedStartTime = true,
            changedDuration = true,
            changedSpeakers = true,
            changedRecordingOptOut = true,
            changedLanguage = true,
            changedTrack = true,
            changedIsNew = true,
            changedIsCanceled = false,
        )
        val comparableCanceledSession = Session(
            guid = "11111111-1111-1111-1111-111111111111",
            changedTitle = false,
            changedSubtitle = false,
            changedRoomName = false,
            changedDayIndex = false,
            changedStartTime = false,
            changedDuration = false,
            changedSpeakers = false,
            changedRecordingOptOut = false,
            changedLanguage = false,
            changedTrack = false,
            changedIsNew = false,
            changedIsCanceled = true,
        )
        val canceledSession = session.cancel()
        assertThat(canceledSession).isEqualTo(comparableCanceledSession)
        assertThat(canceledSession).isNotSameInstanceAs(session)
    }

}
