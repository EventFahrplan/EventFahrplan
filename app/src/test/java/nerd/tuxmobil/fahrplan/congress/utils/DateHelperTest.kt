package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DateHelperTest {

    @Test
    fun getDayOfMonthWithLeapYearDay() {
        // Thursday, February 28, 2019 11:59:59 PM UTC
        assertThat(DateHelper.getDayOfMonth(1551312000000)).isEqualTo(28)
    }

    @Test
    fun getDayOfMonthWithDayAfterLeapYear() {
        // Friday, March 1, 2019 12:00:00 AM UTC
        assertThat(DateHelper.getDayOfMonth(1551398400000)).isEqualTo(1)
    }

    @Test
    fun getLocalDate() {
        val date = LocalDate.of(2019, Month.AUGUST, 21)
        assertThat(DateHelper.getLocalDate("2019-08-21", "yyyy-MM-dd")).isEqualTo(date)
    }

    @Test
    fun getDayStartsAtDate() {
        val date = LocalDate.of(2019, Month.AUGUST, 23)
        val startsAtDate = ZonedDateTime.of(2019, 8, 23, 0, 0, 0, 0, ZoneOffset.UTC)
        assertThat(DateHelper.getDayStartsAtDate(date, ZoneOffset.UTC)).isEqualTo(startsAtDate)
    }

    @Test
    fun getDayEndsAtDate() {
        val date = ZonedDateTime.of(2019, 8, 23, 13, 42, 49, 0, ZoneOffset.UTC)
        val endsAtDate = ZonedDateTime.of(2019, 8, 23, 23, 59, 59, 0, ZoneOffset.UTC)
        assertThat(DateHelper.getDayEndsAtDate(date)).isEqualTo(endsAtDate)
    }

    @Test
    fun getDurationMinutes() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 0, 0, 0, ZoneOffset.UTC)
        val endsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 30, 13, 0, ZoneOffset.UTC)
        assertThat(DateHelper.getDurationMinutes(startsAtDate, endsAtDate)).isEqualTo(30L)
    }

    @Test
    fun getMinutesOfDay() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 27, 6, 30, 0, 0, ZoneOffset.UTC)
        assertThat(DateHelper.getMinuteOfDay(startsAtDate)).isEqualTo(390)
    }

}
