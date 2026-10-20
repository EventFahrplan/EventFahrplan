package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.network.models.Session
import info.metadude.kotlin.library.schedule.v2.models.Day
import info.metadude.kotlin.library.schedule.v2.models.EventState
import info.metadude.kotlin.library.schedule.v2.models.Image
import info.metadude.kotlin.library.schedule.v2.models.LocalizableString
import info.metadude.kotlin.library.schedule.v2.models.Location
import info.metadude.kotlin.library.schedule.v2.models.MetaEvent
import info.metadude.kotlin.library.schedule.v2.models.MetaEventType
import info.metadude.kotlin.library.schedule.v2.models.Participant
import info.metadude.kotlin.library.schedule.v2.models.ParticipantRole
import info.metadude.kotlin.library.schedule.v2.models.RecordingMode
import info.metadude.kotlin.library.schedule.v2.models.Reference
import info.metadude.kotlin.library.schedule.v2.models.ReferenceService
import info.metadude.kotlin.library.schedule.v2.models.ReferenceType
import info.metadude.kotlin.library.schedule.v2.models.Room
import info.metadude.kotlin.library.schedule.v2.models.RoomFeatures
import info.metadude.kotlin.library.schedule.v2.models.RoomReference
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEvent
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEventType
import info.metadude.kotlin.library.schedule.v2.models.TrackReference
import org.junit.jupiter.api.Test
import org.threeten.bp.OffsetDateTime
import kotlin.uuid.Uuid

class EventExtensionsTest {

    private val startsAt = OffsetDateTime.parse("2025-12-27T10:00:00+01:00")

    private val day = Day(
        index = 1,
        date = startsAt.toLocalDate(),
        start = startsAt.minusHours(1),
        end = startsAt.plusHours(12),
    )

    private val room = Room(
        guid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
        name = "Hall A",
    )

    private val breakEvent = MetaEvent(
        end = startsAt.plusMinutes(15),
        guid = Uuid.parse("00000000-0000-0000-0000-000000000003"),
        room = RoomReference(room.guid),
        start = startsAt,
        subtitle = "Tea and Coffee",
        title = "Break",
        type = MetaEventType.BREAK,
    )

    private val talkEvent = ScheduledEvent(
        abstractText = LocalizableString.Text("Lorem ipsum dolor"),
        attachments = listOf(
            Reference(
                url = "https://example.com/attachment.pdf",
                title = "Attachment",
                service = ReferenceService.UNKNOWN,
                type = ReferenceType.SLIDES,
            )
        ),
        code = "c0815",
        description = LocalizableString.Text("Lorem ipsum dolor sit amet"),
        end = startsAt.plusHours(1),
        guid = Uuid.parse("00000000-0000-0000-0000-000000000004"),
        image = Image(url = "https://example.com/image.png"),
        languages = listOf("en"),
        links = listOf(
            Reference(
                url = "https://gitlab.com",
                title = "GitLab",
                service = ReferenceService.GITLAB,
                type = ReferenceType.ORIGIN,
            ),
            Reference(
                url = "https://pretalx.org/event/feedback",
                title = "Feedback",
                service = ReferenceService.UNKNOWN,
                type = ReferenceType.FEEDBACK,
            )
        ),
        location = Location(name = "Conference Center"),
        participants = listOf(
            Participant(
                guid = Uuid.parse("00000000-0000-0000-0000-000000000005"),
                name = "John Doe",
                role = ParticipantRole.SPEAKER,
            ),
        ),
        room = RoomReference(room.guid, name = "Outdated room name"),
        slug = "talk",
        start = startsAt,
        state = EventState.CONFIRMED,
        subtitle = "Lorem ipsum",
        title = "Talk",
        track = TrackReference("talk-c0815"),
        type = ScheduledEventType.TALK,
        url = "https://example.com/talk",
    )

    @Test
    fun `toSessionNetworkModel returns Session created from MetaEvent`() {
        val actual = breakEvent.toSessionNetworkModel(
            day = day,
            room = room,
            roomIndex = 0,
            trackName = "",
        )
        val expected = Session(
            sessionId = "00000000-0000-0000-0000-000000000003",
            sessionGuid = "00000000-0000-0000-0000-000000000003",
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `toSessionNetworkModel returns Session created from ScheduledEvent`() {
        val actual = talkEvent.toSessionNetworkModel(
            day = day,
            room = room,
            roomIndex = 0,
            trackName = "",
        )
        val startsAt = OffsetDateTime.parse("2025-12-27T10:00:00+01:00")
        val expected = Session(
            sessionId = "00000000-0000-0000-0000-000000000004",
            sessionGuid = "00000000-0000-0000-0000-000000000004",
            abstractt = "Lorem ipsum dolor",
            dayIndex = 1,
            dateText = "2025-12-27",
            dateUTC = startsAt.toInstant().toEpochMilli(),
            description = "Lorem ipsum dolor sit amet",
            duration = Duration.ofHours(1),
            feedbackUrl = "https://pretalx.org/event/feedback",
            hasAlarm = false,
            isHighlight = false,
            language = "en",
            links = "[Attachment](https://example.com/attachment.pdf),[GitLab](https://gitlab.com)",
            recordingLicense = "",
            recordingOptOut = !Session.RECORDING_OPT_OUT_OFF,
            roomName = "Hall A",
            roomGuid = "00000000-0000-0000-0000-000000000001",
            roomIndex = 0,
            speakers = "John Doe",
            startTime = Duration.ofHours(10),
            slug = "talk",
            subtitle = "Lorem ipsum",
            timeZoneOffset = startsAt.offset.totalSeconds,
            title = "Talk",
            type = "TALK",
            url = "https://example.com/talk",
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `toSessionNetworkModel sets recordingOptOut unless recording is by default`() {
        val expected = mapOf(
            RecordingMode.RECORD_BY_DEFAULT to false,
            RecordingMode.NOT_RECORDED_BY_DEFAULT to true,
            RecordingMode.UNKNOWN to true,
            RecordingMode.RECORDING_FORBIDDEN to true,
            RecordingMode.RECORDING_NOT_POSSIBLE to true,
        )
        val actual = RecordingMode.entries.associateWith { mode ->
            talkEvent.toSessionNetworkModel(
                day = day,
                room = room.copy(features = RoomFeatures(recording = mode)),
                roomIndex = 0,
                trackName = "",
            ).recordingOptOut
        }
        assertThat(actual).isEqualTo(expected)
    }

}
