package info.metadude.android.eventfahrplan.database.models

/**
 * Database model representing a lecture, a workshop or any similar time-framed happening.
 */
data class Session(

        val sessionId: String,
        val guid: String,
        val abstractt: String = "",
        val dayIndex: Int = 0,          // XML values start with 1
        val date: String = "",
        val dateUTC: Long = 0,
        val description: String = "",
        val duration: Int = 0,          // minutes
        val hasAlarm: Boolean = false,
        val isHighlight: Boolean = false,
        val language: String = "",
        val links: String = "",
        val relativeStartTime: Int = 0, // minutes since conference start
        val recordingLicense: String = "",
        val recordingOptOut: Boolean = RECORDING_OPT_OUT_OFF,
        val room: String = "",
        val roomIndex: Int = 0,
        val speakers: String = "",
        val startTime: Int = 0,         // minutes since day start
        val slug: String = "",
        val subtitle: String = "",
        val timeZoneOffset: Int? = null, // seconds
        val title: String = "",
        val track: String = "",
        val type: String = "",
        val url: String = "",

        val changedDay: Boolean = false,
        val changedDuration: Boolean = false,
        val changedIsCanceled: Boolean = false,
        val changedIsNew: Boolean = false,
        val changedLanguage: Boolean = false,
        val changedRecordingOptOut: Boolean = false,
        val changedRoom: Boolean = false,
        val changedSpeakers: Boolean = false,
        val changedSubtitle: Boolean = false,
        val changedTime: Boolean = false,
        val changedTitle: Boolean = false,
        val changedTrack: Boolean = false

) {

    companion object {
        const val RECORDING_OPT_OUT_ON = true
        const val RECORDING_OPT_OUT_OFF = false
    }

}
