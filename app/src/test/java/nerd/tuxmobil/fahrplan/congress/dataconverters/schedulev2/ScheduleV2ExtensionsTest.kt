package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator
import info.metadude.kotlin.library.schedule.v2.models.Conference
import info.metadude.kotlin.library.schedule.v2.models.Day
import info.metadude.kotlin.library.schedule.v2.models.Generator
import info.metadude.kotlin.library.schedule.v2.models.Schedule
import info.metadude.kotlin.library.schedule.v2.models.ScheduleV2
import info.metadude.kotlin.library.schedule.v2.models.TimeZone
import org.junit.jupiter.api.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

class ScheduleV2ExtensionsTest {

    @Test
    fun `toMetaNetworkModel returns Meta`() {
        val startsAt = OffsetDateTime.parse("2026-12-27T10:00:00+01:00")
        val conference = Conference(
            acronym = "test",
            title = "Test conference",
            start = startsAt.minusHours(1),
            end = startsAt.plusHours(12),
            rooms = emptyList(),
            days = listOf(
                Day(
                    index = 1,
                    date = startsAt.toLocalDate(),
                    start = startsAt.minusHours(1),
                    end = startsAt.plusHours(12),
                )
            ),
            timeZone = TimeZone(ZoneId.of("Europe/Berlin")),
        )
        val schedule = Schedule(
            baseUrl = null,
            conference = conference,
            events = emptyList(),
            persons = emptyList(),
            version = "0.9",
        )
        val generator = Generator(
            name = "pretalx",
            url = "https://pretalx.eu",
            version = "3.0.0",
        )
        val scheduleV2 = ScheduleV2(
            generator = generator,
            schedule = schedule,
            schema = "https://example.com/schema",
        )
        val actualMeta = scheduleV2.toMetaNetworkModel(
            responseETag = "abc123",
            responseLastModifiedAt = "2026-12-31T23:59:59+01:00",
        )
        val expectedMeta = Meta(
            scheduleGenerator = ScheduleGenerator(
                name = "pretalx",
                version = "3.0.0",
            ),
            httpHeader = HttpHeader(
                eTag = "abc123",
                lastModified = "2026-12-31T23:59:59+01:00",
            ),
            numDays = 1,
            title = "Test conference",
            timeZoneName = "Europe/Berlin",
            version = "0.9",
        )
        assertThat(actualMeta).isEqualTo(expectedMeta)
    }

}