package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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
    fun getDurationMinutes() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 0, 0, 0, ZoneOffset.UTC)
        val endsAtDate = ZonedDateTime.of(2019, 8, 25, 12, 30, 13, 0, ZoneOffset.UTC)
        assertThat(DateHelper.getDurationMinutes(startsAtDate, endsAtDate)).isEqualTo(30L)
    }

    @Test
    fun getMinuteOfDayZonedInput() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 27, 6, 30, 0, 0, ZoneOffset.ofHours(4))
        assertThat(DateHelper.getMinuteOfDay(startsAtDate)).isEqualTo((6 - 4) * 60 + 30)
    }

    @Test
    fun getMinuteOfDayUTCInput() {
        val startsAtDate = ZonedDateTime.of(2019, 8, 27, 6, 30, 0, 0, ZoneOffset.ofHours(4))
        assertThat(DateHelper.getMinuteOfDay(startsAtDate.toInstant().toEpochMilli())).isEqualTo((6 - 4) * 60 + 30)
    }
}
