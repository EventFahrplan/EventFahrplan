package info.metadude.android.eventfahrplan.network.temporal

import info.metadude.android.eventfahrplan.network.temporal.DurationParser.getMinutes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DurationParserTest {

    @Test
    fun `duration in minutes`() {
        assertThat(getMinutes("5")).isEqualTo(5)
        assertThat(getMinutes("10")).isEqualTo(10)
        assertThat(getMinutes("15")).isEqualTo(15)
        assertThat(getMinutes("30")).isEqualTo(30)
        assertThat(getMinutes("45")).isEqualTo(45)
        assertThat(getMinutes("60")).isEqualTo(60)
        assertThat(getMinutes("90")).isEqualTo(90)
        assertThat(getMinutes("120")).isEqualTo(120)
        assertThat(getMinutes("1680")).isEqualTo(1680)
        assertThat(getMinutes("2910")).isEqualTo(2910)
        assertThat(getMinutes("7230")).isEqualTo(7230)
    }

    @Test
    fun `duration in hours and minutes`() {
        assertThat(getMinutes("0:05")).isEqualTo(5)
        assertThat(getMinutes("00:05")).isEqualTo(5)
        assertThat(getMinutes("0:10")).isEqualTo(10)
        assertThat(getMinutes("00:10")).isEqualTo(10)
        assertThat(getMinutes("0:15")).isEqualTo(15)
        assertThat(getMinutes("00:15")).isEqualTo(15)
        assertThat(getMinutes("0:30")).isEqualTo(30)
        assertThat(getMinutes("00:30")).isEqualTo(30)
        assertThat(getMinutes("0:45")).isEqualTo(45)
        assertThat(getMinutes("00:45")).isEqualTo(45)
        assertThat(getMinutes("1:00")).isEqualTo(60)
        assertThat(getMinutes("01:00")).isEqualTo(60)
        assertThat(getMinutes("1:30")).isEqualTo(90)
        assertThat(getMinutes("01:30")).isEqualTo(90)
        assertThat(getMinutes("2:00")).isEqualTo(120)
        assertThat(getMinutes("02:00")).isEqualTo(120)
        assertThat(getMinutes("28:00")).isEqualTo(1680)
        assertThat(getMinutes("48:30")).isEqualTo(2910)
        assertThat(getMinutes("120:30")).isEqualTo(7230)
    }

    @Test
    fun `duration in days and hours and minutes`() {
        assertThat(getMinutes("0:00:05")).isEqualTo(5)
        assertThat(getMinutes("0:00:10")).isEqualTo(10)
        assertThat(getMinutes("0:00:15")).isEqualTo(15)
        assertThat(getMinutes("0:00:30")).isEqualTo(30)
        assertThat(getMinutes("0:00:45")).isEqualTo(45)
        assertThat(getMinutes("0:01:00")).isEqualTo(60)
        assertThat(getMinutes("0:01:30")).isEqualTo(90)
        assertThat(getMinutes("0:02:00")).isEqualTo(120)
        assertThat(getMinutes("1:04:00")).isEqualTo(1680)
        assertThat(getMinutes("2:00:30")).isEqualTo(2910)
        assertThat(getMinutes("5:00:30")).isEqualTo(7230)
    }

}
