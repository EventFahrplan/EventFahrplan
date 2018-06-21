package nerd.tuxmobil.fahrplan.congress.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DateInfoTest {

    @Test
    fun isEqualToWithUnequalDayIndex() {
        val dateInfo1 = DateInfo(1, "2018-06-23")
        val dateInfo2 = DateInfo(2, "2018-06-23")
        assertThat(dateInfo1).isNotEqualTo(dateInfo2)
    }

    @Test
    fun isEqualToWithUnequalDate() {
        val dateInfo1 = DateInfo(3, "2018-06-24")
        val dateInfo2 = DateInfo(3, "2018-06-25")
        assertThat(dateInfo1).isNotEqualTo(dateInfo2)
    }

    @Test
    fun isEqualToWithEqualObjects() {
        val dateInfo1 = DateInfo(1, "2018-06-29")
        val dateInfo2 = DateInfo(1, "2018-06-29")
        assertThat(dateInfo1).isEqualTo(dateInfo2)
    }

}
