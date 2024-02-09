package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.kotlin.library.engelsystem.models.Shift
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class ShiftsExtensionsTest {

    private lateinit var startsAt: ZonedDateTime
    private lateinit var endsAt: ZonedDateTime
    private lateinit var dayRanges: List<DayRange>

    @BeforeEach
    fun setUp() {
        val day = Moment.parseDate("2019-08-23")
        startsAt = day.toZonedDateTime(ZoneOffset.UTC)
        endsAt = day.endOfDay().toZonedDateTime(ZoneOffset.UTC)
        dayRanges = listOf(DayRange(day, day))
    }

    @Test
    fun `cropToDayRangesExtent returns empty list if no shifts are present`() {
        assertThat(emptyList<Shift>().cropToDayRangesExtent(dayRanges)).isEmpty()
    }

    @Test
    fun `cropToDayRangesExtent returns the shift if the shift start matches the day range start`() {
        val shift = Shift(startsAtDate = startsAt)
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).hasSize(1)
    }

    @Test
    fun `cropToDayRangesExtent returns empty list if the shift starts before day range start`() {
        val shift = Shift(startsAtDate = startsAt.minusSeconds(1))
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).isEmpty()
    }

    @Test
    fun `cropToDayRangesExtent returns the shift if the shift start matches the day range end`() {
        val shift = Shift(startsAtDate = endsAt)
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).hasSize(1)
    }

    @Test
    fun `cropToDayRangesExtent returns empty list if the shift starts after the day range end`() {
        val shift = Shift(startsAtDate = endsAt.plusSeconds(1))
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).isEmpty()
    }

}
