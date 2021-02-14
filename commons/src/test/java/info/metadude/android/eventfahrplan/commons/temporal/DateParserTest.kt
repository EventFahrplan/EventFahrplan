package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test

class DateParserTest {

    @Test
    fun `parseTimeZoneOffset returns negative integer for negative time zone offset`() {
        assertThat(DateParser.parseTimeZoneOffset("1980-01-01T02:00:00-01:00")).isEqualTo(-3600)
    }

    @Test
    fun `parseTimeZoneOffset returns positive integer for positive time zone offset`() {
        assertThat(DateParser.parseTimeZoneOffset("1980-01-01T02:00:00+01:00")).isEqualTo(3600)
    }

    @Test
    fun `parseTimeZoneOffset returns 0 for UTC offset`() {
        assertThat(DateParser.parseTimeZoneOffset("1980-01-01T02:00:00Z")).isEqualTo(0)
    }

    @Test
    fun `parseTimeZoneOffset fails when time zone offset is missing`() {
        try {
            DateParser.parseTimeZoneOffset("1970-01-01T03:00:00")
            fail("Failure expected because malformed date string should not be parsed.")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).startsWith("Error parsing time zone offset from: '1970-01-01T03:00:00'.")
        }
    }

}
