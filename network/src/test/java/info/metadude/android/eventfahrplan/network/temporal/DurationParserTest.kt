package info.metadude.android.eventfahrplan.network.temporal

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.network.temporal.DurationParser.getMinutes
import org.junit.jupiter.api.Test

class DurationParserTest {

    @Test
    fun `duration in minutes`() {
        assertThat(getMinutes("5").toWholeMinutes()).isEqualTo(5)
        assertThat(getMinutes("10").toWholeMinutes()).isEqualTo(10)
        assertThat(getMinutes("15").toWholeMinutes()).isEqualTo(15)
        assertThat(getMinutes("30").toWholeMinutes()).isEqualTo(30)
        assertThat(getMinutes("45").toWholeMinutes()).isEqualTo(45)
        assertThat(getMinutes("60").toWholeMinutes()).isEqualTo(60)
        assertThat(getMinutes("90").toWholeMinutes()).isEqualTo(90)
        assertThat(getMinutes("120").toWholeMinutes()).isEqualTo(120)
        assertThat(getMinutes("1680").toWholeMinutes()).isEqualTo(1680)
        assertThat(getMinutes("2910").toWholeMinutes()).isEqualTo(2910)
        assertThat(getMinutes("7230").toWholeMinutes()).isEqualTo(7230)
    }

    @Test
    fun `duration in hours and minutes`() {
        assertThat(getMinutes("0:05").toWholeMinutes()).isEqualTo(5)
        assertThat(getMinutes("00:05").toWholeMinutes()).isEqualTo(5)
        assertThat(getMinutes("0:10").toWholeMinutes()).isEqualTo(10)
        assertThat(getMinutes("00:10").toWholeMinutes()).isEqualTo(10)
        assertThat(getMinutes("0:15").toWholeMinutes()).isEqualTo(15)
        assertThat(getMinutes("00:15").toWholeMinutes()).isEqualTo(15)
        assertThat(getMinutes("0:30").toWholeMinutes()).isEqualTo(30)
        assertThat(getMinutes("00:30").toWholeMinutes()).isEqualTo(30)
        assertThat(getMinutes("0:45").toWholeMinutes()).isEqualTo(45)
        assertThat(getMinutes("00:45").toWholeMinutes()).isEqualTo(45)
        assertThat(getMinutes("1:00").toWholeMinutes()).isEqualTo(60)
        assertThat(getMinutes("01:00").toWholeMinutes()).isEqualTo(60)
        assertThat(getMinutes("1:30").toWholeMinutes()).isEqualTo(90)
        assertThat(getMinutes("01:30").toWholeMinutes()).isEqualTo(90)
        assertThat(getMinutes("2:00").toWholeMinutes()).isEqualTo(120)
        assertThat(getMinutes("02:00").toWholeMinutes()).isEqualTo(120)
        assertThat(getMinutes("28:00").toWholeMinutes()).isEqualTo(1680)
        assertThat(getMinutes("48:30").toWholeMinutes()).isEqualTo(2910)
        assertThat(getMinutes("120:30").toWholeMinutes()).isEqualTo(7230)
    }

    @Test
    fun `duration in days and hours and minutes`() {
        assertThat(getMinutes("0:00:05").toWholeMinutes()).isEqualTo(5)
        assertThat(getMinutes("0:00:10").toWholeMinutes()).isEqualTo(10)
        assertThat(getMinutes("0:00:15").toWholeMinutes()).isEqualTo(15)
        assertThat(getMinutes("0:00:30").toWholeMinutes()).isEqualTo(30)
        assertThat(getMinutes("0:00:45").toWholeMinutes()).isEqualTo(45)
        assertThat(getMinutes("0:01:00").toWholeMinutes()).isEqualTo(60)
        assertThat(getMinutes("0:01:30").toWholeMinutes()).isEqualTo(90)
        assertThat(getMinutes("0:02:00").toWholeMinutes()).isEqualTo(120)
        assertThat(getMinutes("1:04:00").toWholeMinutes()).isEqualTo(1680)
        assertThat(getMinutes("2:00:30").toWholeMinutes()).isEqualTo(2910)
        assertThat(getMinutes("5:00:30").toWholeMinutes()).isEqualTo(7230)
    }

}
