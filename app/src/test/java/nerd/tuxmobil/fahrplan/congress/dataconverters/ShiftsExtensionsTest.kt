package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.models.DayRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class ShiftsExtensionsTest {

    private lateinit var startsAt: ZonedDateTime
    private lateinit var endsAt: ZonedDateTime
    private lateinit var dayRanges: List<DayRange>

    @Before
    fun setUp() {
        val day = Moment("2019-08-23")
        startsAt = day.toZonedDateTime(ZoneOffset.UTC)
        endsAt = day.endOfDay().toZonedDateTime(ZoneOffset.UTC)
        dayRanges = listOf(DayRange(day, day))
    }

    @Test
    fun cropToDayRangesExtentWithEmptyList() {
        assertThat(emptyList<Shift>().cropToDayRangesExtent(dayRanges)).isEmpty()
    }

    @Test
    fun cropToDayRangesExtentWithShiftWithinDayRanges() {
        val shift = Shift(startsAt = startsAt)
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).hasSize(1)
    }

    @Test
    fun cropToDayRangesExtentWithShiftBeforeDayRanges() {
        val shift = Shift(startsAt = startsAt.minusSeconds(1))
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).isEmpty()
    }

    @Test
    fun cropToDayRangesExtentWithShiftAtTheEndOfTheDayRanges() {
        val shift = Shift(startsAt = endsAt)
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).hasSize(1)
    }

    @Test
    fun cropToDayRangesExtentWithShiftAfterDayRanges() {
        val shift = Shift(startsAt = endsAt.plusSeconds(1))
        assertThat(listOf(shift).cropToDayRangesExtent(dayRanges)).isEmpty()
    }

}
