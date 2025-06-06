package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.commons.temporal.Duration

/**
 * Database model representing a lecture, a workshop or any similar time-framed happening.
 */
data class Session(

        val sessionId: String,
        val abstractt: String = "",
        val dayIndex: Int = 0,          // XML values start with 1
        val dateText: String = "",
        val dateUTC: Long = 0,
        val description: String = "",
        val duration: Duration = Duration.ZERO, // minutes
        val feedbackUrl: String? = null,
        val hasAlarm: Boolean = false,
        val isHighlight: Boolean = false,
        val language: String = "",
        val links: String = "",
        val relativeStartTime: Duration = Duration.ZERO, // minutes since conference start
        val recordingLicense: String = "",
        val recordingOptOut: Boolean = RECORDING_OPT_OUT_OFF,
        val roomName: String = "",
        val roomIdentifier: String = "",
        val roomIndex: Int = 0,
        val speakers: String = "",
        val startTime: Duration = Duration.ZERO, // minutes since day start
        val slug: String = "",
        val subtitle: String = "",
        val timeZoneOffset: Int? = null, // seconds
        val title: String = "",
        val track: String = "",
        val type: String = "",
        val url: String = "",

        val changedDayIndex: Boolean = false,
        val changedDuration: Boolean = false,
        val changedIsCanceled: Boolean = false,
        val changedIsNew: Boolean = false,
        val changedLanguage: Boolean = false,
        val changedRecordingOptOut: Boolean = false,
        val changedRoomName: Boolean = false,
        val changedSpeakers: Boolean = false,
        val changedStartTime: Boolean = false,
        val changedSubtitle: Boolean = false,
        val changedTitle: Boolean = false,
        val changedTrack: Boolean = false

) {

    companion object {
        const val RECORDING_OPT_OUT_ON = true
        const val RECORDING_OPT_OUT_OFF = false
    }

    /**
     * Keep in sync with [nerd.tuxmobil.fahrplan.congress.models.Session.isChanged].
     */
    val isChanged: Boolean
        get() = changedTitle ||
                changedSubtitle ||
                changedRoomName ||
                changedDayIndex ||
                changedStartTime ||
                changedDuration ||
                changedSpeakers ||
                changedLanguage ||
                changedRecordingOptOut ||
                changedTrack

}
