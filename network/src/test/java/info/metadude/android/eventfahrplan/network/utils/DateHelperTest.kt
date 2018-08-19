package info.metadude.android.eventfahrplan.network.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class DateHelperTest {

    private val calendar: Calendar = Calendar.getInstance()

    @Test
    fun shiftByDays() {
        calendar.set(2016, 1, 29, 0, 0, 0) // 29.2.2016
        val date = calendar.time
        calendar.set(2016, 2, 1, 0, 0, 0) // 1.3.2016
        val shiftedDate = calendar.time
        assertThat(shiftByDays(date, 1)).isEqualTo(shiftedDate)
    }

}
