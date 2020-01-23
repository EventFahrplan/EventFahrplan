package info.metadude.android.eventfahrplan.network.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DateParserTest {

    @Test
    fun `getDateTime date and time`() {
        assertThat(DateParser.getDateTime("2019-01-01T00:00:00Z")).isEqualTo(1546300800000)
    }

    @Test
    fun `getDateTime only date`() {
        assertThat(DateParser.getDateTime("2019-01-01")).isEqualTo(1546300800000)
    }

    @Test
    fun `getDayChange date and time`() {
        assertThat(DateParser.getDayChange("2019-01-01T00:00:00Z")).isEqualTo(0)
        assertThat(DateParser.getDayChange("2019-01-01T01:01:00Z")).isEqualTo(61)
    }

    @Test
    fun `getDayChange only date`() {
        assertThat(DateParser.getDayChange("2019-01-01")).isEqualTo(0)
    }
}
