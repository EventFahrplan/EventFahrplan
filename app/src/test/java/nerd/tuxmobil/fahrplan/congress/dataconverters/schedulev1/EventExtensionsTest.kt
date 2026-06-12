@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("SameParameterValue")

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.kotlin.library.schedule.v1.models.Day
import info.metadude.kotlin.library.schedule.v1.models.Event
import info.metadude.kotlin.library.schedule.v1.models.Link
import info.metadude.kotlin.library.schedule.v1.models.Person
import info.metadude.kotlin.library.schedule.v1.models.Room
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.threeten.bp.Duration as ThreeTenDuration

class EventExtensionsTest {

    @Test
    fun `toSessionNetworkModel maps event fields to a network session`() {
        val roomName = "One"
        val room = roomOf(roomName)
        val event = eventOf(
            id = 4711,
            guid = "00000000-0000-0000-0000-000000000123",
            startsAt = OffsetDateTime.parse("2025-12-27T09:30:00+01:00"),
            startsAtHourMinute = LocalTime.of(9, 30),
            duration = ThreeTenDuration.ofMinutes(45),
            abstractText = " Abstract ",
            description = " Description ",
            roomName = room.name,
            persons = listOf(
                personOf(
                    guid = "00000000-0000-0000-0000-000000000001",
                    name = " Ada Lovelace ",
                ),
                personOf(
                    guid = "00000000-0000-0000-0000-000000000002",
                    name = "Grace Hopper",
                    publicName = " Rear Admiral Grace Hopper ",
                ),
            ),
            title = " Title ",
            subtitle = " Subtitle ",
            track = " Track ",
            type = " Type ",
            languages = " EN ",
            url = " https://example.com/session ",
            links = listOf(Link(url = "https://example.com", title = " Example ")),
            feedbackUrl = " https://example.com/feedback ",
            slug = " session-slug ",
            recordingLicense = " CC-BY ",
            doNotRecord = true,
        )
        val day = dayOf(
            index = 2,
            date = LocalDate.parse("2025-12-27"),
            startsAt = OffsetDateTime.parse("2025-12-27T11:00:00+01:00"),
            endsAt = OffsetDateTime.parse("2025-12-28T06:00:00+01:00"),
            rooms = mapOf(room.name to listOf(event)),
        )
        val session = event.toSessionNetworkModel(
            day = day,
            roomGuid = room.guid.toString(),
            roomIndex = 7,
        )

        assertThat(session.sessionId).isEqualTo("4711")
        assertThat(session.sessionGuid).isEqualTo("00000000-0000-0000-0000-000000000123")
        assertThat(session.dayIndex).isEqualTo(2)
        assertThat(session.dateText).isEqualTo("2025-12-27")
        val startsAt = OffsetDateTime.parse("2025-12-27T09:30:00+01:00")
        assertThat(session.dateUTC).isEqualTo(startsAt.toInstant().toEpochMilli())
        assertThat(session.abstractt).isEqualTo("Abstract")
        assertThat(session.description).isEqualTo("Description")
        assertThat(session.duration).isEqualTo(Duration.ofMinutes(45))
        assertThat(session.roomName).isEqualTo("One")
        assertThat(session.roomGuid).isEqualTo("00000000-4444-3333-2222-000000000000")
        assertThat(session.roomIndex).isEqualTo(7)
        assertThat(session.speakers).isEqualTo("Ada Lovelace;Rear Admiral Grace Hopper")
        assertThat(session.startTime).isEqualTo(Duration.ofMinutes(570))
        assertThat(session.title).isEqualTo("Title")
        assertThat(session.subtitle).isEqualTo("Subtitle")
        assertThat(session.track).isEqualTo("Track")
        assertThat(session.type).isEqualTo("Type")
        assertThat(session.language).isEqualTo("EN")
        assertThat(session.url).isEqualTo("https://example.com/session")
        assertThat(session.links).isEqualTo("[Example](https://example.com)")
        assertThat(session.feedbackUrl).isEqualTo("https://example.com/feedback")
        assertThat(session.timeZoneOffset).isEqualTo(3600)
        assertThat(session.slug).isEqualTo("session-slug")
        assertThat(session.recordingLicense).isEqualTo("CC-BY")
        assertThat(session.recordingOptOut).isTrue()
    }

    private fun eventOf(
        id: Int = 1,
        guid: String = "00000000-0000-0000-0000-000000000999",
        startsAt: OffsetDateTime,
        startsAtHourMinute: LocalTime,
        duration: ThreeTenDuration = ThreeTenDuration.ZERO,
        abstractText: String = "",
        description: String = "",
        roomName: String = "",
        persons: List<Person> = emptyList(),
        title: String = "",
        subtitle: String = "",
        track: String = "",
        type: String = "",
        languages: String = "",
        url: String = "",
        links: List<Link> = emptyList(),
        feedbackUrl: String? = null,
        slug: String = "",
        recordingLicense: String = "",
        doNotRecord: Boolean? = false,
    ) = Event(
        guid = Uuid.parse(guid),
        code = "",
        id = id,
        logo = "",
        date = startsAt,
        start = startsAtHourMinute,
        duration = duration,
        room = roomName,
        slug = slug,
        title = title,
        subtitle = subtitle,
        language = languages,
        track = track,
        type = type,
        abstractText = abstractText,
        description = description,
        recordingLicense = recordingLicense,
        persons = persons,
        url = url,
        links = links,
        originUrl = "",
        feedbackUrl = feedbackUrl,
        doNotRecord = doNotRecord,
        doNotStream = false,
        attachments = emptyList(),
    )

    private fun roomOf(name: String) = Room(
        guid = Uuid.parse("00000000-4444-3333-2222-000000000000"),
        name = name,
    )

    private fun dayOf(
        index: Int,
        date: LocalDate,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime,
        rooms: Map<String, List<Event>> = emptyMap(),
    ) = Day(
        index = index,
        date = date,
        dayStart = startsAt,
        dayEnd = endsAt,
        rooms = rooms,
    )

    private fun personOf(
        guid: String,
        name: String,
        publicName: String? = null,
    ) = Person(
        guid = Uuid.parse(guid),
        name = name,
        publicName = publicName,
    )

}
