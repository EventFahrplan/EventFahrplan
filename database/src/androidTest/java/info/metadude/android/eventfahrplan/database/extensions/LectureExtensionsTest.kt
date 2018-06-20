package info.metadude.android.eventfahrplan.database.extensions

import android.support.test.runner.AndroidJUnit4
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns.*
import info.metadude.android.eventfahrplan.database.models.Lecture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LectureExtensionsTest {

    @Test
    fun toContentValues() {
        val lecture = Lecture(
                eventId = "7331",
                abstractt = "Lorem ipsum",
                dayIndex = 3,
                date = "2015-08-13",
                dateUTC = 1439478900000L,
                description = "Lorem ipsum dolor sit amet",
                duration = 45,
                hasAlarm = true,
                isHighlight = true,
                language = "en",
                links = "[Website](https://www.example.com/path)",
                relativeStartTime = 1035,
                recordingLicense = "CC 0",
                recordingOptOut = Lecture.RECORDING_OPT_OUT_ON,
                room = "Simulacron-3",
                roomIndex = 17,
                speakers = "John Doe; Noah Doe",
                startTime = 1036,
                slug = "lorem",
                subtitle = "My subtitle",
                title = "My title",
                track = "Security & Hacking",
                type = "tutorial",

                changedDay = true,
                changedDuration = true,
                changedIsCanceled = true,
                changedIsNew = true,
                changedLanguage = true,
                changedRecordingOptOut = true,
                changedRoom = true,
                changedSpeakers = true,
                changedSubtitle = true,
                changedTime = true,
                changedTitle = true,
                changedTrack = true
        )
        val values = lecture.toContentValues()
        assertThat(values.getAsInteger(EVENT_ID)).isEqualTo(7331)
        assertThat(values.getAsString(ABSTRACT)).isEqualTo("Lorem ipsum")
        assertThat(values.getAsInteger(DAY)).isEqualTo(3)
        assertThat(values.getAsString(DATE)).isEqualTo("2015-08-13")
        assertThat(values.getAsLong(DATE_UTC)).isEqualTo(1439478900000L)
        assertThat(values.getAsString(DESCR)).isEqualTo("Lorem ipsum dolor sit amet")
        assertThat(values.getAsInteger(DURATION)).isEqualTo(45)
        // The value of "hasAlarms" is not persisted.
        // The value of "isHighlight" is not persisted.
        assertThat(values.getAsString(LANG)).isEqualTo("en")
        assertThat(values.getAsString(LINKS)).isEqualTo("[Website](https://www.example.com/path)")
        assertThat(values.getAsInteger(REL_START)).isEqualTo(1035)
        assertThat(values.getAsString(REC_LICENSE)).isEqualTo("CC 0")
        assertThat(values.getAsBoolean(REC_OPTOUT)).isEqualTo(Lecture.RECORDING_OPT_OUT_ON)
        assertThat(values.getAsString(ROOM)).isEqualTo("Simulacron-3")
        assertThat(values.getAsInteger(ROOM_IDX)).isEqualTo(17)
        assertThat(values.getAsString(SPEAKERS)).isEqualTo("John Doe; Noah Doe")
        assertThat(values.getAsInteger(START)).isEqualTo(1036)
        assertThat(values.getAsString(SLUG)).isEqualTo("lorem")
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(TRACK)).isEqualTo("Security & Hacking")
        assertThat(values.getAsString(TYPE)).isEqualTo("tutorial")

        assertThat(values.getAsBoolean(CHANGED_DAY)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_DURATION)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_IS_CANCELED)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_IS_NEW)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_LANGUAGE)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_RECORDING_OPTOUT)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_ROOM)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_SPEAKERS)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_SUBTITLE)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_TIME)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_TITLE)).isEqualTo(true)
        assertThat(values.getAsBoolean(CHANGED_TRACK)).isEqualTo(true)
    }

}
