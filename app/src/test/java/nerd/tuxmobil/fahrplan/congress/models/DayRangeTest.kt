package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DayRangeTest {

    private lateinit var dayRange: DayRange

    @Before
    fun setUp() {
        val day1 = Moment.parseDate("2019-12-27")
        val day2 = Moment.parseDate("2019-12-30")
        dayRange = DayRange(day1, day2)
    }

    @Test
    fun containsWithDateTimeBeforeStartsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 26, 23, 59, 59, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isFalse()
    }

    @Test
    fun containsWithDateTimeMatchingStartsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 27, 0, 0, 0, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isTrue()
    }

    @Test
    fun containsWithDateTimeAfterStartsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 27, 0, 0, 1, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isTrue()
    }

    @Test
    fun containsWithDateTimeBeforeEndsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 30, 23, 59, 58, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isTrue()
    }

    @Test
    fun containsWithDateTimeMatchingEndsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 30, 23, 59, 59, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isTrue()
    }

    @Test
    fun containsWithDateTimeAfterEndsAt() {
        val dateTime = ZonedDateTime.of(2019, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)
        assertThat(dayRange.contains(dateTime)).isFalse()
    }

}
