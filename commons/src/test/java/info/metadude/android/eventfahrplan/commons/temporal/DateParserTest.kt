package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.format.DateTimeParseException

class DateParserTest {

    @Test
    fun `parseDateTime returns milliseconds for 2019 date and time with time zone and offset`() {
        assertThat(DateParser.parseDateTime("2019-01-01T00:00:00Z")).isEqualTo(1546300800000)
    }

    @Test
    fun `parseDateTime returns milliseconds for 1970 epoch date and time UTC`() {
        assertThat(DateParser.parseDateTime("1970-01-01T00:00:00Z")).isEqualTo(0)
    }

    @Test
    fun `parseDateTime returns milliseconds for 1970 epoch date and time with time zone with zero offset`() {
        assertThat(DateParser.parseDateTime("1970-01-01T00:00:00+00:00")).isEqualTo(0)
    }

    @Test
    fun `parseDateTime returns milliseconds for short after 1970 epoch date and time with time zone with offset`() {
        assertThat(DateParser.parseDateTime("1970-01-01T02:00:00+01:00")).isEqualTo(3600000)
    }

    @Test
    fun `parseDateTime returns milliseconds for 2016 date and time with time zone with offset`() {
        // Test format of date / times ever seen in schedule.xml files.
        assertThat(DateParser.parseDateTime("2016-09-14T14:30:00+02:00")).isEqualTo(1473856200000)
    }

    @Test
    fun `parseDateTime returns milliseconds for 2016 date and time UTC`() {
        // Test format of date / times ever seen in schedule.xml files.
        assertThat(DateParser.parseDateTime("2016-09-14T12:30:00Z")).isEqualTo(1473856200000)
    }

    @Test
    fun `parseDateTime fails when time zone offset is missing without a colon`() {
        // Test format of date / times ever seen in schedule.xml files.
        try {
            DateParser.parseDateTime("2016-09-14T14:30:00+0200")
            fail("Failure expected because malformed date string should not be parsed.")
        } catch (e: DateTimeParseException) {
            assertThat(e.message).startsWith("Text '2016-09-14T14:30:00+0200' could not be parsed")
        }
    }

    @Test
    fun `parseDateTime fails when time zone offset is missing`() {
        try {
            DateParser.parseDateTime("1970-01-01T03:00:00")
            fail("Failure expected because malformed date string should not be parsed.")
        } catch (e: DateTimeException) {
            assertThat(e.message).startsWith("Unable to obtain Instant from TemporalAccessor: DateTimeBuilder")
        }
    }

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
