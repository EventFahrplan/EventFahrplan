package info.metadude.android.eventfahrplan.network.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.format.DateTimeParseException

class DateParserTest {

    @Test
    fun `getDateTime returns milliseconds for 2019 date and time with time zone and offset`() {
        assertThat(DateParser.getDateTime("2019-01-01T00:00:00Z")).isEqualTo(1546300800000)
    }

    @Test
    fun `getDateTime returns milliseconds for 1970 epoch date and time UTC`() {
        assertThat(DateParser.getDateTime("1970-01-01T00:00:00Z")).isEqualTo(0)
    }

    @Test
    fun `getDateTime returns milliseconds for 1970 epoch date and time with time zone with zero offset`() {
        assertThat(DateParser.getDateTime("1970-01-01T00:00:00+00:00")).isEqualTo(0)
    }

    @Test
    fun `getDateTime returns milliseconds for short after 1970 epoch date and time with time zone with offset`() {
        assertThat(DateParser.getDateTime("1970-01-01T02:00:00+01:00")).isEqualTo(3600000)
    }

    @Test
    fun `getDateTime returns milliseconds for 2016 date and time with time zone with offset`() {
        // Test format of date / times ever seen in schedule.xml files.
        assertThat(DateParser.getDateTime("2016-09-14T14:30:00+02:00")).isEqualTo(1473856200000)
    }

    @Test
    fun `getDateTime returns milliseconds for 2016 date and time UTC`() {
        // Test format of date / times ever seen in schedule.xml files.
        assertThat(DateParser.getDateTime("2016-09-14T12:30:00Z")).isEqualTo(1473856200000)
    }

    @Test
    fun `getDateTime fails when time zone offset is missing without a colon`() {
        // Test format of date / times ever seen in schedule.xml files.
        try {
            DateParser.getDateTime("2016-09-14T14:30:00+0200")
            fail("Failure expected because malformed date string should not be parsed.")
        } catch (e: DateTimeParseException) {
            assertThat(e.message).startsWith("Text '2016-09-14T14:30:00+0200' could not be parsed")
        }
    }

    @Test
    fun `getDateTime fails when time zone offset is missing`() {
        try {
            DateParser.getDateTime("1970-01-01T03:00:00")
            fail("Failure expected because malformed date string should not be parsed.")
        } catch (e: DateTimeException) {
            assertThat(e.message).startsWith("Unable to obtain Instant from TemporalAccessor: DateTimeBuilder")
        }
    }

    @Test
    fun `getDateTime returns milliseconds for first day in 2019 date`() {
        assertThat(DateParser.getDateTime("2019-01-01")).isEqualTo(1546300800000)
    }

    @Test
    fun `getDateTime returns milliseconds for first day in 2020 date`() {
        // If there are off-by-one errors, e.g. parsing for 0-based month-numbers, this may fail.
        assertThat(DateParser.getDateTime("2020-01-01")).isEqualTo(1577836800000)
    }

    @Test
    fun `getDateTime returns milliseconds for leap year date`() {
        // If there are off-by-one errors, e.g. parsing for 0-based month-numbers, this may fail.
        assertThat(DateParser.getDateTime("2020-02-29")).isEqualTo(1582934400000)
    }

    @Test
    fun `getDateTime returns milliseconds for last day in 2020 date`() {
        // If there are off-by-one errors, e.g. parsing for 0-based month-numbers, this may fail.
        assertThat(DateParser.getDateTime("2020-12-31")).isEqualTo(1609372800000)
    }

    @Test
    fun `getDayChange returns minutes of a day for first day in 2019 date and time`() {
        assertThat(DateParser.getDayChange("2019-01-01T00:00:00Z")).isEqualTo(0)
    }

    @Test
    fun `getDayChange returns minutes of a day for first hour and minute in 2019 date and time`() {
        assertThat(DateParser.getDayChange("2019-01-01T01:01:00Z")).isEqualTo(61)
    }

    @Test
    fun `getDayChange returns minutes of a day for first day in 2019 date`() {
        assertThat(DateParser.getDayChange("2019-01-01")).isEqualTo(0)
    }

    @Test
    fun `getDayChange returns minutes of a day for last CET minute of 2020 leap year`() {
        // DST change CET/CEST 2020 was on 03-29 (but rather boring with explicit offsets)
        assertThat(DateParser.getDayChange("2020-03-29T01:59:00+01:00")).isEqualTo(59)
    }

    @Test
    fun `getDayChange returns minutes of a day for first CEST minute of 2020 leap year`() {
        // DST change CET/CEST 2020 was on 03-29 (but rather boring with explicit offsets)
        assertThat(DateParser.getDayChange("2020-03-29T03:00:00+02:00")).isEqualTo(60)
    }

    @Test
    fun `getMinutes returns 0 minutes for 00_00`() {
        assertThat(DateParser.getMinutes("00:00")).isEqualTo(0)
    }

    @Test
    fun `getMinutes returns 30 minutes for 00_30`() {
        assertThat(DateParser.getMinutes("00:30")).isEqualTo(30)
    }

    @Test
    fun `getMinutes returns 90 minutes for 1_30`() {
        assertThat(DateParser.getMinutes("1:30")).isEqualTo(90)
    }

    @Test
    fun `getMinutes returns 135 minutes for 2_15_00`() {
        assertThat(DateParser.getMinutes("2:15:00")).isEqualTo(135)
    }

    @Test
    fun `getMinutes returns 300 minutes for 05_00`() {
        assertThat(DateParser.getMinutes("05:00")).isEqualTo(300)
    }

    @Test
    fun `getMinutes returns 540 minutes for 09_00_00`() {
        // <day_change> value from Pentabarf schedule.xml
        assertThat(DateParser.getMinutes("09:00:00")).isEqualTo(540)
    }

    @Test
    fun `getMinutes returns 1020 minutes for 17_00`() {
        assertThat(DateParser.getMinutes("17:00")).isEqualTo(1020)
    }

    @Test
    fun `getMinutes returns 1439 minutes for 23_59`() {
        assertThat(DateParser.getMinutes("23:59")).isEqualTo(1439)
    }

}
