package nerd.tuxmobil.fahrplan.congress.utils

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

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

}
