package info.metadude.android.eventfahrplan.database.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ABSTRACT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_CANCELED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_NEW
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_LANGUAGE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_RECORDING_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_ROOM
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_UTC
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DESCR
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.GUID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LANG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LINKS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_LICENSE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REL_START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_IDX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SLUG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TYPE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.URL
import info.metadude.android.eventfahrplan.database.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionExtensionsTest {

    @Test
    fun toContentValues() {
        val session = Session(
                sessionId = "7331",
                guid = "11111111-1111-1111-1111-111111111111",
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
                recordingOptOut = Session.RECORDING_OPT_OUT_ON,
                room = "Simulacron-3",
                roomIndex = 17,
                speakers = "John Doe; Noah Doe",
                startTime = 1036,
                slug = "lorem",
                subtitle = "My subtitle",
                title = "My title",
                track = "Security & Hacking",
                type = "tutorial",
                url = "https://talks.mrmcd.net/2018/talk/V3FUNG",

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
        val values = session.toContentValues()
        assertThat(values.getAsInteger(SESSION_ID)).isEqualTo(7331)
        assertThat(values.getAsString(GUID)).isEqualTo("11111111-1111-1111-1111-111111111111")
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
        assertThat(values.getAsBoolean(REC_OPTOUT)).isEqualTo(Session.RECORDING_OPT_OUT_ON)
        assertThat(values.getAsString(ROOM)).isEqualTo("Simulacron-3")
        assertThat(values.getAsInteger(ROOM_IDX)).isEqualTo(17)
        assertThat(values.getAsString(SPEAKERS)).isEqualTo("John Doe; Noah Doe")
        assertThat(values.getAsInteger(START)).isEqualTo(1036)
        assertThat(values.getAsString(SLUG)).isEqualTo("lorem")
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(TRACK)).isEqualTo("Security & Hacking")
        assertThat(values.getAsString(TYPE)).isEqualTo("tutorial")
        assertThat(values.getAsString(URL)).isEqualTo("https://talks.mrmcd.net/2018/talk/V3FUNG")

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
