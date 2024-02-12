package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.NoLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class ShiftExtensionsTest {

    @Test
    fun `oneBasedDayIndex returns 1 if day range spans 1 day`() {
        val zoneOffset = ZoneOffset.ofHours(2)
        val day = Moment.parseDate("2019-08-21")
        val dayRanges = listOf(DayRange(day))

        val shiftStart = day.toZonedDateTime(zoneOffset).plusSeconds(59)
        val shiftEnd = shiftStart.plusSeconds(59)
        val shift = Shift(startsAtDate = shiftStart, endsAtDate = shiftEnd)

        assertThat(shift.oneBasedDayIndex(NoLogging, dayRanges)).isEqualTo(1)
    }

    @Test
    fun `oneBasedDayIndex returns 2 if day range spans 2 days`() {
        val zoneOffset = ZoneOffset.ofHours(2)
        val day1 = Moment.parseDate("2019-08-21")
        val day2 = Moment.parseDate("2019-08-22")
        val dayRanges = listOf(DayRange(day1), DayRange(day2))

        val shiftStart = day2.toZonedDateTime(zoneOffset).plusSeconds(59)
        val shiftEnd = shiftStart.plusSeconds(59)
        val shift = Shift(startsAtDate = shiftStart, endsAtDate = shiftEnd)

        assertThat(shift.oneBasedDayIndex(NoLogging, dayRanges)).isEqualTo(2)
    }

    @Test
    fun `descriptionText returns empty string if no property is set`() {
        assertThat(Shift().descriptionText).isEmpty()
    }

    @Test
    fun `descriptionText returns empty string if only locationName property is set`() {
        assertThat(Shift(locationName = "Room 23").descriptionText).isEmpty()
    }

    @Test
    fun `descriptionText returns formatted HTML link derived from locationUrl property`() {
        assertThat(Shift(locationUrl = "https://example.com").descriptionText).isEqualTo("<a href=\"https://example.com\">https://example.com</a>")
    }

    @Test
    fun `descriptionText returns string as set in the locationDescription property`() {
        assertThat(Shift(locationDescription = "The large green room.").descriptionText).isEqualTo("The large green room.")
    }

    @Test
    fun `descriptionText returns user comment formatted in italics`() {
        assertThat(Shift(userComment = "Don't forget a warm jacket.").descriptionText).isEqualTo("_Don't forget a warm jacket._")
    }

    @Test
    fun `descriptionText returns formatted HTML link composed from relevant shift properties`() {
        val shift = Shift(
                locationName = "Room 42",
                locationUrl = "https://conference.org",
                locationDescription = "The small orange room.",
                userComment = "Take a bottle of water with you"
        )
        val text = "<a href=\"https://conference.org\">https://conference.org</a>\n\nThe small orange room.\n\n_Take a bottle of water with you_"
        assertThat(shift.descriptionText).isEqualTo(text)
    }

    @Test
    fun `toSessionAppModel sessionizes a shift and handles time zone correctly`() {
        val day = Moment.parseDate("2019-01-02")
        val startsAt = day.toZonedDateTime(ZoneOffset.ofHours(4)) // 2019-01-02 04:00 with offset +4 => still  2019-01-02 00:00Z
        val shift = Shift(
                startsAtDate = startsAt,
                timeZoneOffset = ZoneOffset.ofHours(2) // for whatever reason someone sets timeZoneOffset different than startsAtDate's offset
        )
        val dayRange = DayRange(day)
        val session = shift.toSessionAppModel(NoLogging, "", listOf(dayRange))
        assertThat(session.startTime).isEqualTo(0) // nevertheless, we still expect sessions time data to be based on UTC
        assertThat(session.relStartTime).isEqualTo(0)
    }

    @Test
    fun `toSessionAppModel sessionizes a shift and duration returns correct value in minutes`() {
        val day = Moment.parseDate("2019-08-25")
        val startsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 0, 0, 0, ZoneOffset.UTC)
        val endsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 30, 13, 0, ZoneOffset.UTC)
        val dayRange = DayRange(day)
        val shift = Shift(startsAtDate = startsAtDate, endsAtDate = endsAtDate)
        assertThat(shift.toSessionAppModel(NoLogging, "", listOf(dayRange)).duration).isEqualTo(30)
    }

}
