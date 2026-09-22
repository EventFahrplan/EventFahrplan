package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.network.models.Session
import info.metadude.kotlin.library.schedule.v2.models.Conference
import info.metadude.kotlin.library.schedule.v2.models.Day
import info.metadude.kotlin.library.schedule.v2.models.Event
import info.metadude.kotlin.library.schedule.v2.models.EventState
import info.metadude.kotlin.library.schedule.v2.models.MetaEvent
import info.metadude.kotlin.library.schedule.v2.models.MetaEventType
import info.metadude.kotlin.library.schedule.v2.models.Room
import info.metadude.kotlin.library.schedule.v2.models.RoomReference
import info.metadude.kotlin.library.schedule.v2.models.Schedule
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEvent
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEventType
import info.metadude.kotlin.library.schedule.v2.models.Track
import info.metadude.kotlin.library.schedule.v2.models.TrackReference
import nerd.tuxmobil.fahrplan.congress.NoLogging
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import kotlin.uuid.Uuid

class ScheduleExtensionsTest {

    private val startsAt = OffsetDateTime.parse("2025-12-27T10:00:00+01:00")

    private val room1 = Room(
        guid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
        name = "Hall A",
    )

    private val room2 = Room(
        guid = Uuid.parse("00000000-0000-0000-0000-000000000002"),
        name = "Hall B",
    )

    private val breakEvent = MetaEvent(
        guid = Uuid.parse("00000000-0000-0000-0000-000000000003"),
        end = startsAt.plusMinutes(15),
        room = RoomReference(room1.guid),
        start = startsAt,
        title = "Break",
        type = MetaEventType.BREAK,
    )
    private val talkEvent = ScheduledEvent(
        guid = Uuid.parse("00000000-0000-0000-0000-000000000004"),
        abstractText = null,
        end = startsAt.plusHours(1),
        links = emptyList(),
        participants = emptyList(),
        room = RoomReference(room2.guid, name = "Outdated room name"),
        slug = "talk-society",
        start = startsAt,
        subtitle = null,
        title = "Talk",
        track = TrackReference("society"),
        type = ScheduledEventType.TALK,
        url = "https://example.com/talk",
    )

    private fun createDays() = listOf(
        Day(
            index = 1,
            date = startsAt.toLocalDate(),
            start = startsAt.minusHours(1),
            end = startsAt.plusHours(12),
        ),
    )

    private fun createTracks() = listOf(
        Track(
            slug = "society",
            name = "Society",
        ),
    )

    private fun createSchedule(
        days: List<Day>,
        rooms: List<Room>,
        tracks: List<Track>,
        events: List<Event>,
    ) = Schedule(
        conference = Conference(
            acronym = "test",
            days = days,
            end = startsAt.plusHours(12),
            rooms = rooms,
            start = startsAt.minusHours(1),
            title = "Test conference",
            tracks = tracks,
        ),
        events = events,
        persons = emptyList(),
        version = "1.0.0",
    )

    @Test
    fun `toSessionsNetworkModel forwards associated day, room, roomIndex, trackName`() {
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room2),
            tracks = createTracks(),
            events = listOf(talkEvent),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { day, room, roomIndex, trackName ->
                assertThat(day).isEqualTo(Day(
                    date = LocalDate.parse("2025-12-27"),
                    end = OffsetDateTime.parse("2025-12-27T22:00:00+01:00"),
                    index = 1,
                    start = OffsetDateTime.parse("2025-12-27T09:00:00+01:00"),
                ))
                assertThat(room).isEqualTo(Room(
                    guid = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                    name = "Hall B",
                ))
                assertThat(roomIndex).isEqualTo(1)
                assertThat(trackName).isEqualTo("Society")
                Session("fake")
            }
        )
        assertThat(actualSessions).isEqualTo(listOf(Session("fake")))
    }

    @Test
    fun `toSessionsNetworkModel returns empty list when days is empty`() {
        val schedule = createSchedule(
            days = emptyList(),
            rooms = listOf(room1, room2),
            tracks = createTracks(),
            events = listOf(talkEvent),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { _, _, _, _ -> Session("fake") }
        )
        assertThat(actualSessions).isEmpty()
    }

    @Test
    fun `toSessionsNetworkModel returns empty list when rooms is empty`() {
        val schedule = createSchedule(
            days = createDays(),
            rooms = emptyList(),
            tracks = createTracks(),
            events = listOf(talkEvent),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { _, _, _, _ -> Session("fake") }
        )
        assertThat(actualSessions).isEmpty()
    }

    @Test
    fun `toSessionsNetworkModel returns empty list when days, rooms, tracks are empty`() {
        val schedule = createSchedule(
            days = emptyList(),
            rooms = emptyList(),
            tracks = createTracks(),
            events = listOf(talkEvent),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { _, _, _, _ -> Session("fake") }
        )
        assertThat(actualSessions).isEmpty()
    }

    @Test
    fun `toSessionsNetworkModel forwards empty track name if tracks are empty`() {
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room2),
            tracks = emptyList(),
            events = listOf(talkEvent),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { _, _, _, trackName ->
                assertThat(trackName).isEmpty()
                Session("fake")
            }
        )
        assertThat(actualSessions).isEqualTo(listOf(Session("fake")))
    }

    @Test
    fun `toSessionsNetworkModel associates room properties and the related room index`() {
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room2),
            tracks = createTracks(),
            events = listOf(breakEvent, talkEvent),
        )
        val sessions = schedule.toSessionsNetworkModel(NoLogging)

        assertThat(sessions).hasSize(1)

        val talkSession = sessions.single { it.title == "Talk" }
        assertThat(talkSession.roomGuid).isEqualTo("00000000-0000-0000-0000-000000000002")
        assertThat(talkSession.roomName).isEqualTo("Hall B")
        assertThat(talkSession.roomIndex).isEqualTo(1)
    }

    @Test
    fun `toSessionsNetworkModel skips events with missing or unknown rooms`() {
        val unknownRoom = RoomReference(Uuid.parse("00000000-0000-0000-0000-000000000099"))
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room2),
            tracks = createTracks(),
            events = listOf(
                talkEvent.copy(room = null),
                talkEvent.copy(room = unknownRoom),
                breakEvent.copy(room = null),
                breakEvent.copy(room = unknownRoom),
            ),
        )
        val sessions = schedule.toSessionsNetworkModel(NoLogging)
        assertThat(sessions).isEmpty()
    }

    @Test
    fun `toSessionsNetworkModel does not duplicate events and retains first room index for duplicate room definitions`() {
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room1.copy(name = "Duplicate"), room2),
            tracks = createTracks(),
            events = listOf(talkEvent, talkEvent.copy(
                title = "Talk 2",
                room = RoomReference(room2.guid, name = "Duplicate")),
            ),
        )
        val sessions = schedule.toSessionsNetworkModel(NoLogging)

        assertThat(sessions).hasSize(2)
        assertThat(sessions.single { it.title == "Talk" }.roomName).isEqualTo("Hall B")
        assertThat(sessions.single { it.title == "Talk" }.roomIndex).isEqualTo(1)
    }

    @Test
    fun `toSessionsNetworkModel filters events which are not confirmed nor unknown`() {
        val event1 = talkEvent.copy(state = EventState.CONFIRMED)
        val event2 = talkEvent.copy(state = EventState.CANCELED)
        val event3 = talkEvent.copy(state = EventState.UNKNOWN)
        val schedule = createSchedule(
            days = createDays(),
            rooms = listOf(room1, room2),
            tracks = createTracks(),
            events = listOf(event1, event2, event3),
        )
        val actualSessions = schedule.toSessionsNetworkModel(
            logging = NoLogging,
            toSessionNetworkModel = { _, _, _, _ ->
                Session("fake")
            }
        )
        assertThat(actualSessions).hasSize(2)
    }

}
