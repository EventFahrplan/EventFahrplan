package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.models.Session.Companion.RECORDING_OPT_OUT_ON
import nerd.tuxmobil.fahrplan.congress.models.DateInfo
import nerd.tuxmobil.fahrplan.congress.models.Room
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

class SessionExtensionsTest {

    @Test
    fun `shiftRoomIndexOnDays shifts the room index by 1 if the day index is contained in the given set`() {
        val session = SessionNetworkModel(
            guid = "11111111-1111-1111-1111-111111111111",
            dayIndex = 3,
            roomIndex = 17,
        )
        val dayIndices = setOf(3)
        val shiftedSession = session.shiftRoomIndexOnDays(dayIndices)
        assertThat(shiftedSession.roomIndex).isEqualTo(18)
        assertThat(shiftedSession).isNotSameInstanceAs(session)
    }

    @Test
    fun `shiftRoomIndexOnDays does not shift the room index if the day index is not contained in the given set`() {
        val session = SessionNetworkModel(
            guid = "11111111-1111-1111-1111-111111111111",
            dayIndex = 3,
            roomIndex = 17,
        )
        val dayIndices = setOf(1, 2)
        val shiftedSession = session.shiftRoomIndexOnDays(dayIndices)
        assertThat(shiftedSession.roomIndex).isEqualTo(17)
        assertThat(shiftedSession).isSameInstanceAs(session)
    }

    @Test
    fun `shiftRoomIndexOnDays does not shift the room index if the given set is empty`() {
        val session = SessionNetworkModel(
            guid = "11111111-1111-1111-1111-111111111111",
            dayIndex = 3,
            roomIndex = 17,
        )
        val dayIndices = emptySet<Int>()
        val shiftedSession = session.shiftRoomIndexOnDays(dayIndices)
        assertThat(shiftedSession.roomIndex).isEqualTo(17)
        assertThat(shiftedSession).isSameInstanceAs(session)
    }

    @Test
    fun `toSessionAppModel returns an app session derived from a database session`() {
        val databaseModel = SessionDatabaseModel(
                guid = "11111111-1111-1111-1111-111111117331",
                abstractt = "Lorem ipsum",
                dayIndex = 3,
                dateText = "2015-08-13",
                dateUTC = 1439478900000L,
                description = "Lorem ipsum dolor sit amet",
                duration = 45,
                feedbackUrl = "https://talks.mrmcd.net/2018/talk/V3FUNG/feedback",
                hasAlarm = true,
                isHighlight = true,
                language = "en",
                links = "[Website](https://www.example.com/path)",
                relativeStartTime = 1035,
                recordingLicense = "CC 0",
                recordingOptOut = RECORDING_OPT_OUT_ON,
                roomName = "Simulacron-3",
                roomIdentifier = "88888888-4444-4444-4444-121212121212",
                roomIndex = 17,
                speakers = "John Doe; Noah Doe",
                startTime = 1036,
                slug = "lorem",
                subtitle = "My subtitle",
                timeZoneOffset = 3600,
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
                changedRoomName = true,
                changedSpeakers = true,
                changedStartTime = true,
                changedSubtitle = true,
                changedTitle = true,
                changedTrack = true
        )

        val appModel = SessionAppModel(
            guid = "11111111-1111-1111-1111-111111117331",
            abstractt = "Lorem ipsum",
            dayIndex = 3,
            dateText = "2015-08-13",
            dateUTC = 1439478900000L,
            description = "Lorem ipsum dolor sit amet",
            duration = 45,
            feedbackUrl = "https://talks.mrmcd.net/2018/talk/V3FUNG/feedback",
            hasAlarm = true,
            isHighlight = true,
            language = "en",
            links = "[Website](https://www.example.com/path)",
            relativeStartTime = 1035,
            recordingLicense = "CC 0",
            recordingOptOut = RECORDING_OPT_OUT_ON,
            roomName = "Simulacron-3",
            roomIdentifier = "88888888-4444-4444-4444-121212121212",
            roomIndex = 17,
            speakers = listOf("John Doe", "Noah Doe"),
            startTime = 1036,
            slug = "lorem",
            subtitle = "My subtitle",
            timeZoneOffset = ZoneOffset.ofTotalSeconds(3600),
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
            changedRoomName = true,
            changedSpeakers = true,
            changedSubtitle = true,
            changedStartTime = true,
            changedTitle = true,
            changedTrack = true,
        )
        assertThat(databaseModel.toSessionAppModel()).isEqualTo(appModel)
    }

    @Test
    fun `toSessionDatabaseModel and toSessionNetworkModel convert a network session forth and back`() {
        val networkModel = SessionNetworkModel(
            guid = "11111111-1111-1111-1111-111111117331",
                abstractt = "Lorem ipsum",
                dayIndex = 3,
                dateText = "2015-08-13",
                dateUTC = 1439478900000L,
                description = "Lorem ipsum dolor sit amet",
                duration = 45,
                feedbackUrl = "https://talks.mrmcd.net/2018/talk/V3FUNG/feedback",
                hasAlarm = true,
                isHighlight = true,
                language = "en",
                links = "[Website](https://www.example.com/path)",
                relativeStartTime = 1035,
                recordingLicense = "CC 0",
                recordingOptOut = RECORDING_OPT_OUT_ON,
                roomName = "Simulacron-3",
                roomGuid = "88888888-4444-4444-4444-121212121212",
                roomIndex = 17,
                speakers = "John Doe; Noah Doe",
                startTime = 1036,
                slug = "lorem",
                subtitle = "My subtitle",
                timeZoneOffset = 3600,
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
                changedRoomName = true,
                changedSpeakers = true,
                changedSubtitle = true,
                changedStartTime = true,
                changedTitle = true,
                changedTrack = true
        )
        assertThat(networkModel.toSessionDatabaseModel().toSessionNetworkModel()).isEqualTo(networkModel)
    }

    @Test
    fun `toRoom returns a Room instance`() {
        val session = SessionAppModel(
            guid = "",
            roomIdentifier = "bccb8a5b-a268-4f17-90b9-b5966f5e34d8",
            roomName = "Stage F",
        )
        val expectedRoom = Room(identifier = "bccb8a5b-a268-4f17-90b9-b5966f5e34d8", name = "Stage F")
        assertThat(session.toRoom()).isEqualTo(expectedRoom)
    }

    @Test
    fun `toDateInfo returns a DateInfo object derived from a session`() {
        val session = SessionDatabaseModel(
            guid = "",
            dateText = "2015-08-13",
            dayIndex = 3,
        )
        val dateInfo = DateInfo(3, Moment.parseDate("2015-08-13"))
        assertThat(session.toDateInfo()).isEqualTo(dateInfo)
    }

    @Test
    fun `toDayRanges returns a list of day ranges derived from a list of sessions`() {
        val session0 = SessionDatabaseModel(
            guid = "",
            dateText = "2019-08-02",
            dayIndex = 2,
        )
        val session1 = SessionDatabaseModel(
            guid = "",
            dateText = "2019-08-01",
            dayIndex = 1,
        )
        val session1Copy = SessionDatabaseModel(
            guid = "",
            dateText = "2019-08-01",
            dayIndex = 1,
        )

        val sessions = listOf(session0, session1, session1Copy)
        val dayRanges = sessions.toDayRanges()

        assertThat(dayRanges.size).isEqualTo(2)
        assertThat(dayRanges[0].startsAt.dayOfMonth).isEqualTo(1)
        assertThat(dayRanges[0].startsAt.hour).isEqualTo(0)
        assertThat(dayRanges[1].startsAt.dayOfMonth).isEqualTo(2)
        assertThat(dayRanges[1].startsAt.hour).isEqualTo(0)

        assertThat(dayRanges[0].endsAt.dayOfMonth).isEqualTo(1)
        assertThat(dayRanges[0].endsAt.hour).isEqualTo(23)
        assertThat(dayRanges[1].endsAt.dayOfMonth).isEqualTo(2)
        assertThat(dayRanges[1].endsAt.hour).isEqualTo(23)
    }

    @Test
    fun `toHighlightDatabaseModel returns a Highlight object derived from a session`() {
        val session = SessionAppModel(
            guid = "11111111-1111-1111-1111-111111114723",
            isHighlight = true,
        )
        val highlight = Highlight(guid = "11111111-1111-1111-1111-111111114723", isHighlight = true)
        assertThat(session.toHighlightDatabaseModel()).isEqualTo(highlight)
    }

    @Test
    fun `sanitize moves the subtitle property value to the title property value if empty`() {
        val session = SessionNetworkModel(
            guid = "",
            subtitle = "Lorem ipsum",
            title = "",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            subtitle = "",
            title = "Lorem ipsum",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize clears the subtitle property value if the title property matches`() {
        val session = SessionNetworkModel(
            guid = "",
            subtitle = "Lorem ipsum",
            title = "Lorem ipsum",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            subtitle = "",
            title = "Lorem ipsum",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize keeps the subtitle property value if the title property value differs`() {
        val session = SessionNetworkModel(
            guid = "",
            subtitle = "Dolor sit amet",
            title = "Lorem ipsum",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            subtitle = "Dolor sit amet",
            title = "Lorem ipsum",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize clears the abstractt property value if the description property matches`() {
        val session = SessionNetworkModel(
            guid = "",
            abstractt = "Lorem ipsum",
            description = "Lorem ipsum",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            abstractt = "",
            description = "Lorem ipsum",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize keeps the abstractt property value if the description property value differs`() {
        val session = SessionNetworkModel(
            guid = "",
            abstractt = "Lorem ipsum",
            description = "Dolor sit amet",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            abstractt = "Lorem ipsum",
            description = "Dolor sit amet",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize moves the abstractt property value to the description property value if empty`() {
        val session = SessionNetworkModel(
            guid = "",
            abstractt = "Lorem ipsum",
            description = "",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            abstractt = "",
            description = "Lorem ipsum",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize clears the subtitle property value if the speakers property value matches`() {
        val session = SessionNetworkModel(
            guid = "",
            speakers = "Luke Skywalker",
            subtitle = "Luke Skywalker",
            title = "Some title",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            speakers = "Luke Skywalker",
            subtitle = "",
            title = "Some title",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize keeps the subtitle property value if the speakers property value differs`() {
        val session = SessionNetworkModel(
            guid = "",
            speakers = "Darth Vader",
            subtitle = "Lorem ipsum",
            title = "Some title",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            speakers = "Darth Vader",
            subtitle = "Lorem ipsum",
            title = "Some title",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize converts the language property value to lower case if it is not empty`() {
        val session = SessionNetworkModel(
            guid = "",
            language = "EN",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            language = "en",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize keeps the language property value if it is empty`() {
        val session = SessionNetworkModel(
            guid = "",
            language = "",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            language = "",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize replaces the track property value with the type property value for certain track property values`() {
        val session = SessionNetworkModel(
            guid = "",
            track = "Sendezentrum-Bühne",
            type = "Live",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            track = "Live",
            type = "Live",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize keeps the track property value if the type property is empty`() {
        val session = SessionNetworkModel(
            guid = "",
            track = "Art",
            type = "",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            track = "Art",
            type = "",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize replaces the track property value if roomName is classics and type is Other`() {
        val session = SessionNetworkModel(
            guid = "",
            roomName = "classics",
            track = "",
            type = "Other",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            roomName = "classics",
            track = "Classics",
            type = "Other",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize replaces the track property value if roomName is rC3 Lounge`() {
        val session = SessionNetworkModel(
            guid = "",
            roomName = "rC3 Lounge",
            track = "",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            roomName = "rC3 Lounge",
            track = "Music",
        )
        assertThat(session).isEqualTo(expected)
    }

    @Test
    fun `sanitize copies the non-empty type property value to the track property if empty`() {
        val session = SessionNetworkModel(
            guid = "",
            track = "",
            type = "Workshop",
        ).sanitize()
        val expected = SessionNetworkModel(
            guid = "",
            track = "Workshop",
            type = "Workshop",
        )
        assertThat(session).isEqualTo(expected)
    }

}
