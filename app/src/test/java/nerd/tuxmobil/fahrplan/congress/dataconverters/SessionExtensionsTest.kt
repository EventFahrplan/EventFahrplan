package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.models.Session.Companion.RECORDING_OPT_OUT_ON
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

class SessionExtensionsTest {

    @Test
    fun `shiftRoomIndexOnDays does not shift the room index if day indices are empty`() {
        val session = SessionNetworkModel(dayIndex = 0, roomIndex = 0)
        val dayIndices = emptySet<Int>()
        val modifiedSession = SessionNetworkModel(dayIndex = 0, roomIndex = 0)
        assertThat(session.shiftRoomIndexOnDays(dayIndices)).isEqualTo(modifiedSession)
    }

    @Test
    fun `shiftRoomIndexOnDays shifts the room index if the day index matches`() {
        val session = SessionNetworkModel(dayIndex = 0, roomIndex = 0)
        val dayIndices = setOf(0)
        val modifiedSession = SessionNetworkModel(dayIndex = 0, roomIndex = 1)
        assertThat(session.shiftRoomIndexOnDays(dayIndices)).isEqualTo(modifiedSession)
    }

    @Test
    fun `shiftRoomIndexOnDays does not shifts the room index if the day index does not match`() {
        val session = SessionNetworkModel(dayIndex = 0, roomIndex = 2)
        val dayIndices = setOf(1)
        val modifiedSession = SessionNetworkModel(dayIndex = 0, roomIndex = 2)
        assertThat(session.shiftRoomIndexOnDays(dayIndices)).isEqualTo(modifiedSession)
    }

    @Test
    fun sessionDatabaseModel_toSessionAppModel_toSessionDatabaseModel() {
        val session = SessionDatabaseModel(
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
                recordingOptOut = RECORDING_OPT_OUT_ON,
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
        assertThat(session.toSessionAppModel().toSessionDatabaseModel()).isEqualTo(session)
    }

    @Test
    fun sessionDatabaseModel_toSessionNetworkModel_toSessionDatabaseModel() {
        val session = SessionDatabaseModel(
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
                recordingOptOut = RECORDING_OPT_OUT_ON,
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
        assertThat(session.toSessionNetworkModel().toSessionDatabaseModel()).isEqualTo(session)
    }

    @Test
    fun sessionNetworkModel_toSessionAppModel_toSessionNetworkModel() {
        val session = SessionNetworkModel(
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
                recordingOptOut = RECORDING_OPT_OUT_ON,
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

                changedDayIndex = true,
                changedDuration = true,
                changedIsCanceled = true,
                changedIsNew = true,
                changedLanguage = true,
                changedRecordingOptOut = true,
                changedRoom = true,
                changedSpeakers = true,
                changedSubtitle = true,
                changedStartTime = true,
                changedTitle = true,
                changedTrack = true
        )
        assertThat(session.toSessionAppModel().toSessionNetworkModel()).isEqualTo(session)
    }

    @Test
    fun toDateInfo() {
        val session = SessionDatabaseModel(
                sessionId = "",
                guid = "11111111-1111-1111-1111-111111111111",
                date = "2015-08-13",
                dayIndex = 3
        )
        val dateInfo = DateInfo(3, Moment.parseDate("2015-08-13"))
        assertThat(session.toDateInfo()).isEqualTo(dateInfo)
    }

    @Test
    fun toHighlightDatabaseModel() {
        val session = SessionAppModel("")
        session.sessionId = "4723"
        session.highlight = true
        val highlight = Highlight(sessionId = 4723, isHighlight = true)
        assertThat(session.toHighlightDatabaseModel()).isEqualTo(highlight)
    }

    @Test
    fun sanitizeWithSameTitleAndSubtitle() {
        val session = SessionNetworkModel(
                subtitle = "Lorem ipsum",
                title = "Lorem ipsum"
        ).sanitize()
        val expected = SessionNetworkModel(
                subtitle = "",
                title = "Lorem ipsum"
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithDifferentTitleAndSubtitle() {
        val session = SessionNetworkModel(
                subtitle = "Dolor sit amet",
                title = "Lorem ipsum"
        ).sanitize()
        val expected = SessionNetworkModel(
                subtitle = "Dolor sit amet",
                title = "Lorem ipsum"
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithSameAbstractAndDescription() {
        val session = SessionNetworkModel(
                abstractt = "Lorem ipsum",
                description = "Lorem ipsum"
        ).sanitize()
        val expected = SessionNetworkModel(
                abstractt = "",
                description = "Lorem ipsum"
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithDifferentAbstractAndDescription() {
        val session = SessionNetworkModel(
                abstractt = "Lorem ipsum",
                description = "Dolor sit amet"
        ).sanitize()
        val expected = SessionNetworkModel(
                abstractt = "Lorem ipsum",
                description = "Dolor sit amet"
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithAbstractWithoutDescription() {
        val session = SessionNetworkModel(
                abstractt = "Lorem ipsum",
                description = ""
        ).sanitize()
        val expected = SessionNetworkModel(
                description = "Lorem ipsum"
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithSameSpeakersAndSubtitle() {
        val session = SessionNetworkModel(
                speakers = "Luke Skywalker",
                subtitle = "Luke Skywalker"
        ).sanitize()
        val expected = SessionNetworkModel(
                speakers = "Luke Skywalker",
                subtitle = ""
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun sanitizeWithDifferentSpeakersAndAbstract() {
        val session = SessionNetworkModel(
                speakers = "Darth Vader",
                subtitle = "Lorem ipsum"
        ).sanitize()
        val expected = SessionNetworkModel(
                speakers = "Darth Vader",
                subtitle = "Lorem ipsum"
        )
        assertThat(session).isEqualTo(expected)
    }

}
