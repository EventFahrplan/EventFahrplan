package info.metadude.android.eventfahrplan.database.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource

class SessionTest {

    companion object {
        @JvmStatic
        fun isChangedData() = listOf(
            of(false, false, false, false, false, false, false, false, false, false, false),
            of(true, false, false, false, false, false, false, false, false, false, true),
            of(false, true, false, false, false, false, false, false, false, false, true),
            of(false, false, true, false, false, false, false, false, false, false, true),
            of(false, false, false, true, false, false, false, false, false, false, true),
            of(false, false, false, false, true, false, false, false, false, false, true),
            of(false, false, false, false, false, true, false, false, false, false, true),
            of(false, false, false, false, false, false, true, false, false, false, true),
            of(false, false, false, false, false, false, false, true, false, false, true),
            of(false, false, false, false, false, false, false, false, true, false, true),
        )
    }

    @ParameterizedTest(name = "{index}: changedTitle = {0}, changedSubtitle = {1}, changedRoomName = {2}, changedDayIndex = {3}, changedStartTime = {4}, changedDuration = {5}, changedSpeakers = {6}, changedLanguage = {7}, changedRecordingOptOut = {8}, changedTrack = {9} -> isChanged = {10}")
    @MethodSource("isChangedData")
    fun isChanged(
        changedTitle: Boolean,
        changedSubtitle: Boolean,
        changedRoomName: Boolean,
        changedDay: Boolean,
        changedTime: Boolean,
        changedDuration: Boolean,
        changedSpeakers: Boolean,
        changedLanguage: Boolean,
        changedRecordingOptOut: Boolean,
        changedTrack: Boolean,
        isChanged: Boolean,
    ) {
        val session = Session("").copy(
            changedTitle = changedTitle,
            changedSubtitle = changedSubtitle,
            changedRoomName = changedRoomName,
            changedDayIndex = changedDay,
            changedStartTime = changedTime,
            changedDuration = changedDuration,
            changedSpeakers = changedSpeakers,
            changedLanguage = changedLanguage,
            changedRecordingOptOut = changedRecordingOptOut,
            changedTrack = changedTrack,
        )
        assertThat(session.isChanged).isEqualTo(isChanged)
    }

}
