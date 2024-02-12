package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DateInfoTest {

    @Test
    fun `asserts that DateInfo objects with odd day indexes are not equal`() {
        val dateInfo1 = DateInfo(1, Moment.parseDate("2018-06-23"))
        val dateInfo2 = DateInfo(2, Moment.parseDate("2018-06-23"))
        assertThat(dateInfo1).isNotEqualTo(dateInfo2)
    }

    @Test
    fun `asserts that DateInfo objects with odd dates are not equal`() {
        val dateInfo1 = DateInfo(3, Moment.parseDate("2018-06-24"))
        val dateInfo2 = DateInfo(3, Moment.parseDate("2018-06-25"))
        assertThat(dateInfo1).isNotEqualTo(dateInfo2)
    }

    @Test
    fun `asserts that DateInfo objects with equal properties are equal`() {
        val dateInfo1 = DateInfo(1, Moment.parseDate("2018-06-29"))
        val dateInfo2 = DateInfo(1, Moment.parseDate("2018-06-29"))
        assertThat(dateInfo1).isEqualTo(dateInfo2)
    }

}
