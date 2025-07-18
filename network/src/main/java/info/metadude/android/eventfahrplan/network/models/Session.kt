package info.metadude.android.eventfahrplan.network.models

import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser

/**
 * Network model representing a lecture, a workshop or any similar time-framed happening.
 * Values in this class are parsed from a schedule XML file via [FahrplanParser].
 */
data class Session(

        var sessionId: String = "",
        var abstractt: String = "",
        var dayIndex: Int = 0, // XML values start with 1
        var dateText: String = "",
        var dateUTC: Long = 0,
        var description: String = "",
        var duration: Duration = Duration.ZERO, // minutes
        var feedbackUrl: String? = null,
        var hasAlarm: Boolean = false,
        var isHighlight: Boolean = false,
        var language: String = "",
        var links: String = "",
        var relativeStartTime: Duration = Duration.ZERO,
        var recordingLicense: String = "",
        var recordingOptOut: Boolean = RECORDING_OPT_OUT_OFF,
        var roomName: String = "",
        var roomGuid: String = "",
        var roomIndex: Int = 0,
        var speakers: String = "",
        var startTime: Duration = Duration.ZERO, // minutes since day start
        var slug: String = "",
        var subtitle: String = "",
        var timeZoneOffset: Int? = null, // seconds
        var title: String = "",
        var track: String = "",
        var type: String = "",
        var url: String = "",

        var changedDayIndex: Boolean = false,
        var changedDuration: Boolean = false,
        var changedIsCanceled: Boolean = false,
        var changedIsNew: Boolean = false,
        var changedLanguage: Boolean = false,
        var changedRecordingOptOut: Boolean = false,
        var changedRoomName: Boolean = false,
        var changedSpeakers: Boolean = false,
        var changedStartTime: Boolean = false,
        var changedSubtitle: Boolean = false,
        var changedTitle: Boolean = false,
        var changedTrack: Boolean = false
) {

    companion object {

        const val RECORDING_OPT_OUT_OFF = false

    }

    /**
     * Returns a new session with [changedIsCanceled] set to `true`
     * and all other change flags set to `false`.
     */
    fun cancel() = copy(
        changedIsCanceled = true,
        changedIsNew = false,
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
