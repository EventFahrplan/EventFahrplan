@file:OptIn(ExperimentalUuidApi::class)

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import com.google.common.truth.Truth.assertThat
import info.metadude.kotlin.library.schedule.v1.models.Assembly
import info.metadude.kotlin.library.schedule.v1.models.Conference
import info.metadude.kotlin.library.schedule.v1.models.ConferenceColors
import info.metadude.kotlin.library.schedule.v1.models.ConferenceDate.DateTime
import info.metadude.kotlin.library.schedule.v1.models.Day
import info.metadude.kotlin.library.schedule.v1.models.Event
import info.metadude.kotlin.library.schedule.v1.models.Generator
import info.metadude.kotlin.library.schedule.v1.models.Room
import info.metadude.kotlin.library.schedule.v1.models.RoomFeatures
import info.metadude.kotlin.library.schedule.v1.models.RoomType
import info.metadude.kotlin.library.schedule.v1.models.Schedule
import info.metadude.kotlin.library.schedule.v1.models.ScheduleV1
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator as ScheduleGeneratorNetworkModel
import org.threeten.bp.Duration as ThreeTenDuration

class ScheduleV1ExtensionsTest {

    @Test
    fun `toRoomIndexByRoomName assigns stable indexes in first-seen order`() {
        val conference = conferenceOf(
            days = listOf(
                dayOf(
                    index = 1,
                    date = LocalDate.parse("2025-12-27"),
                    rooms = linkedMapOf(
                        "Hall Z" to emptyList(),
                        "Hall A" to emptyList(),
                    ),
                ),
                dayOf(
                    index = 2,
                    date = LocalDate.parse("2025-12-28"),
                    rooms = linkedMapOf(
                        "Hall A" to emptyList(),
                        "Hall B" to emptyList(),
                    ),
                ),
            ),
            rooms = emptyList(),
        )

        assertThat(conference.toRoomIndexByRoomName())
            .isEqualTo(
                linkedMapOf(
                    "Hall Z" to 0,
                    "Hall A" to 1,
                    "Hall B" to 2,
                )
            )
    }

    @Test
    fun `toSessionsNetworkModel maps events and injects room guids and room indexes`() {
        val day = dayOf(
            index = 1,
            date = LocalDate.parse("2025-12-27"),
            rooms = linkedMapOf(
                "Hall 2" to listOf(
                    eventOf(
                        id = 2,
                        guid = "00000000-0000-2222-0000-000000000000",
                        startsAt = OffsetDateTime.parse("2025-12-27T09:00:00+01:00"),
                        startsAtHoursMinute = LocalTime.of(9, 0),
                        roomName = "Hall 2",
                        title = "Second",
                    ),
                ),
                "Hall 1" to listOf(
                    eventOf(
                        id = 1,
                        guid = "00000000-0000-1111-0000-000000000000",
                        startsAt = OffsetDateTime.parse("2025-12-27T10:00:00+01:00"),
                        startsAtHoursMinute = LocalTime.of(10, 0),
                        roomName = "Hall 1",
                        title = "First",
                    ),
                ),
            ),
        )
        val conference = conferenceOf(
            days = listOf(day),
            rooms = listOf(
                roomOf(
                    name = "Hall 1",
                    guid = "00000000-1111-0000-1111-000000000000",
                ),
                roomOf(
                    name = "Hall 2",
                    guid = "00000000-2222-0000-2222-000000000000",
                ),
            ),
        )

        val sessions = scheduleV1Of(conference).toSessionsNetworkModel()

        assertThat(sessions.map { it.sessionId }).isEqualTo(listOf("2", "1"))
        assertThat(sessions.map { it.roomName }).isEqualTo(listOf("Hall 2", "Hall 1"))
        assertThat(sessions.map { it.roomGuid }).isEqualTo(
            listOf(
                "00000000-2222-0000-2222-000000000000",
                "00000000-1111-0000-1111-000000000000",
            )
        )
        assertThat(sessions.map { it.roomIndex }).isEqualTo(listOf(0, 1))
    }

    @Test
    fun `toMetaNetworkModel maps populated schedule and response header values`() {
        val conference = conferenceOf(
            days = listOf(
                dayOf(
                    index = 1,
                    date = LocalDate.parse("2025-12-27"),
                    rooms = linkedMapOf("Hall 1" to emptyList()),
                ),
                dayOf(
                    index = 2,
                    date = LocalDate.parse("2025-12-28"),
                    rooms = linkedMapOf("Hall 2" to emptyList()),
                ),
            ),
            rooms = emptyList(),
            title = "38C3",
            timeZoneName = ZoneId.of("Europe/Berlin"),
        )

        val meta = scheduleV1Of(
            conference = conference,
            version = "2025.1",
            generator = Generator(name = "pretalx", version = "2025.12"),
        ).toMetaNetworkModel(
            responseETag = "\"abc123\"",
            responseLastModifiedAt = "Wed, 01 Jan 2025 12:34:56 GMT",
        )

        assertThat(meta).isEqualTo(
            MetaNetworkModel(
                scheduleGenerator = ScheduleGeneratorNetworkModel(
                    name = "pretalx",
                    version = "2025.12",
                ),
                httpHeader = HttpHeaderNetworkModel(
                    eTag = "\"abc123\"",
                    lastModified = "Wed, 01 Jan 2025 12:34:56 GMT",
                ),
                numDays = 2,
                title = "38C3",
                timeZoneName = "Europe/Berlin",
                version = "2025.1",
            )
        )
    }

    @Test
    fun `toMetaNetworkModel maps empty and nullable values unchanged`() {
        val conference = conferenceOf(
            days = emptyList(),
            rooms = emptyList(),
            title = "",
            timeZoneName = null,
        )

        val meta = scheduleV1Of(
            conference = conference,
            version = "",
            generator = null,
        ).toMetaNetworkModel(
            responseETag = "",
            responseLastModifiedAt = "",
        )

        assertThat(meta).isEqualTo(
            MetaNetworkModel(
                scheduleGenerator = ScheduleGeneratorNetworkModel(
                    name = null,
                    version = null,
                ),
                httpHeader = HttpHeaderNetworkModel(
                    eTag = "",
                    lastModified = "",
                ),
                numDays = 0,
                title = "",
                timeZoneName = null,
                version = "",
            )
        )
    }

    private fun eventOf(
        id: Int,
        guid: String,
        startsAt: OffsetDateTime,
        startsAtHoursMinute: LocalTime,
        roomName: String,
        title: String,
    ) = Event(
        guid = Uuid.parse(guid),
        code = "",
        id = id,
        logo = "",
        date = startsAt,
        start = startsAtHoursMinute,
        duration = ThreeTenDuration.ofMinutes(30),
        room = roomName,
        slug = "",
        title = title,
        subtitle = "",
        language = "EN",
        track = "",
        type = "",
        abstractText = "",
        description = "",
        recordingLicense = "",
        persons = emptyList(),
        url = "",
        links = emptyList(),
        originUrl = "",
        feedbackUrl = "",
        doNotRecord = false,
        doNotStream = false,
        attachments = emptyList(),
    )

    private fun dayOf(
        index: Int,
        date: LocalDate,
        rooms: LinkedHashMap<String, List<Event>>,
    ) = Day(
        index = index,
        date = date,
        dayStart = date.atTime(6, 0).atOffset(ZoneOffset.ofHours(1)),
        dayEnd = date.atStartOfDay().atOffset(ZoneOffset.ofHours(1)),
        rooms = rooms,
    )

    private fun roomOf(name: String, guid: String): Room {
        return Room(
            name = name,
            slug = "",
            guid = Uuid.parse(guid),
            type = RoomType.UNKNOWN,
            streamId = "",
            description = null,
            capacity = null,
            url = "",
            descriptionEn = "",
            descriptionDe = "",
            features = RoomFeatures(),
            assembly = assemblyOf(),
        )
    }

    private fun assemblyOf() = Assembly(
        name = "",
        slug = "",
        guid = Uuid.parse("99999999-0000-0000-0000-000000000000"),
        url = "",
    )

    private fun conferenceColorsOf() = ConferenceColors()

    private fun conferenceOf(
        days: List<Day>,
        rooms: List<Room>,
        title: String = "",
        timeZoneName: ZoneId? = ZoneId.of("Europe/Berlin"),
    ) = Conference(
        acronym = "",
        title = title,
        description = "",
        start = DateTime(OffsetDateTime.parse("2025-12-27T00:00:00+01:00")),
        end = DateTime(OffsetDateTime.parse("2025-12-28T23:59:00+01:00")),
        daysCount = days.size,
        timeslotDuration = ThreeTenDuration.ofMinutes(15),
        timeZoneName = timeZoneName,
        logo = "",
        colors = conferenceColorsOf(),
        keywords = emptyList(),
        url = "",
        tracks = emptyList(),
        rooms = rooms,
        days = days,
    )

    private fun scheduleV1Of(
        conference: Conference,
        version: String = "1.0",
        generator: Generator? = Generator("", ""),
    ) = ScheduleV1(
        schema = "",
        generator = generator,
        schedule = Schedule(version, "", conference),
    )

}
