package info.metadude.android.eventfahrplan.network.models

import info.metadude.android.eventfahrplan.network.serialization.FahrplanParser

/**
 * Network model representing a lecture, a workshop or any similar time-framed happening.
 * Values in this class are parsed from a schedule XML file via [FahrplanParser].
 */
data class Session(

        var sessionId: String = "",
        var guid: String = "",

        var abstractt: String = "",
        var dayIndex: Int = 0, // XML values start with 1
        var date: String = "",
        var dateUTC: Long = 0,
        var description: String = "",
        var duration: Int = 0, // minutes
        var hasAlarm: Boolean = false,
        var isHighlight: Boolean = false,
        var language: String = "",
        var links: String = "",
        var relativeStartTime: Int = 0,
        var recordingLicense: String = "",
        var recordingOptOut: Boolean = RECORDING_OPT_OUT_OFF,
        var room: String = "",
        var roomIndex: Int = 0,
        var speakers: String = "",
        var startTime: Int = 0, // minutes since day start
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
        var changedRoom: Boolean = false,
        var changedSpeakers: Boolean = false,
        var changedStartTime: Boolean = false,
        var changedSubtitle: Boolean = false,
        var changedTitle: Boolean = false,
        var changedTrack: Boolean = false
) {

    companion object {

        const val RECORDING_OPT_OUT_OFF = false

    }

}
