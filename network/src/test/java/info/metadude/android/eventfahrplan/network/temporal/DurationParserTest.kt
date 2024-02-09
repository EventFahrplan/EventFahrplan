package info.metadude.android.eventfahrplan.network.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DurationParserTest {
    @Test
    fun `duration in minutes`() {
        assertThat(DurationParser.getMinutes("5")).isEqualTo(5)
        assertThat(DurationParser.getMinutes("10")).isEqualTo(10)
        assertThat(DurationParser.getMinutes("15")).isEqualTo(15)
        assertThat(DurationParser.getMinutes("30")).isEqualTo(30)
        assertThat(DurationParser.getMinutes("45")).isEqualTo(45)
        assertThat(DurationParser.getMinutes("60")).isEqualTo(60)
        assertThat(DurationParser.getMinutes("90")).isEqualTo(90)
        assertThat(DurationParser.getMinutes("120")).isEqualTo(120)
        assertThat(DurationParser.getMinutes("1680")).isEqualTo(1680)
        assertThat(DurationParser.getMinutes("2910")).isEqualTo(2910)
        assertThat(DurationParser.getMinutes("7230")).isEqualTo(7230)
    }

    @Test
    fun `duration in hours and minutes`() {
        assertThat(DurationParser.getMinutes("0:05")).isEqualTo(5)
        assertThat(DurationParser.getMinutes("00:05")).isEqualTo(5)
        assertThat(DurationParser.getMinutes("0:10")).isEqualTo(10)
        assertThat(DurationParser.getMinutes("00:10")).isEqualTo(10)
        assertThat(DurationParser.getMinutes("0:15")).isEqualTo(15)
        assertThat(DurationParser.getMinutes("00:15")).isEqualTo(15)
        assertThat(DurationParser.getMinutes("0:30")).isEqualTo(30)
        assertThat(DurationParser.getMinutes("00:30")).isEqualTo(30)
        assertThat(DurationParser.getMinutes("0:45")).isEqualTo(45)
        assertThat(DurationParser.getMinutes("00:45")).isEqualTo(45)
        assertThat(DurationParser.getMinutes("1:00")).isEqualTo(60)
        assertThat(DurationParser.getMinutes("01:00")).isEqualTo(60)
        assertThat(DurationParser.getMinutes("1:30")).isEqualTo(90)
        assertThat(DurationParser.getMinutes("01:30")).isEqualTo(90)
        assertThat(DurationParser.getMinutes("2:00")).isEqualTo(120)
        assertThat(DurationParser.getMinutes("02:00")).isEqualTo(120)
        assertThat(DurationParser.getMinutes("28:00")).isEqualTo(1680)
        assertThat(DurationParser.getMinutes("48:30")).isEqualTo(2910)
        assertThat(DurationParser.getMinutes("120:30")).isEqualTo(7230)
    }

    @Test
    fun `duration in days and hours and minutes`() {
        assertThat(DurationParser.getMinutes("0:00:05")).isEqualTo(5)
        assertThat(DurationParser.getMinutes("0:00:10")).isEqualTo(10)
        assertThat(DurationParser.getMinutes("0:00:15")).isEqualTo(15)
        assertThat(DurationParser.getMinutes("0:00:30")).isEqualTo(30)
        assertThat(DurationParser.getMinutes("0:00:45")).isEqualTo(45)
        assertThat(DurationParser.getMinutes("0:01:00")).isEqualTo(60)
        assertThat(DurationParser.getMinutes("0:01:30")).isEqualTo(90)
        assertThat(DurationParser.getMinutes("0:02:00")).isEqualTo(120)
        assertThat(DurationParser.getMinutes("1:04:00")).isEqualTo(1680)
        assertThat(DurationParser.getMinutes("2:00:30")).isEqualTo(2910)
        assertThat(DurationParser.getMinutes("5:00:30")).isEqualTo(7230)
    }
}
