package info.metadude.android.eventfahrplan.commons.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class DateFormatterTest {
    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()
    private val moment = Moment("2019-01-22")
    private val timestamp = moment.toMilliseconds()

    @Before
    fun resetTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
    }

    @After
    fun resetSystemDefaults() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun getFormattedTime() {
        Locale.setDefault(Locale("en", "US"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
        assertThat(DateFormatter.newInstance().getFormattedTime(timestamp)).isEqualTo("1:00 AM")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(DateFormatter.newInstance().getFormattedTime(timestamp)).isEqualTo("2:00 PM")

        Locale.setDefault(Locale("de", "DE"))
        assertThat(DateFormatter.newInstance().getFormattedTime(timestamp)).isEqualTo("14:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"))
        assertThat(DateFormatter.newInstance().getFormattedTime(timestamp)).isEqualTo("06:00")
    }

    @Test
    fun getFormattedTimeNumbersOnly() {
        Locale.setDefault(Locale("en", "US"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
        assertThat(DateFormatter.newInstance().getFormattedTime24Hour(moment)).isEqualTo("01:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(DateFormatter.newInstance().getFormattedTime24Hour(moment)).isEqualTo("14:00")

        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"))
        assertThat(DateFormatter.newInstance().getFormattedTime24Hour(moment)).isEqualTo("06:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(DateFormatter.newInstance().getFormattedTime24Hour(moment)).isEqualTo("14:00")
    }

    @Test
    fun getFormattedDate() {
        Locale.setDefault(Locale.US)
        assertThat(DateFormatter.newInstance().getFormattedDate(timestamp)).isEqualTo("1/22/19")

        Locale.setDefault(Locale.GERMANY)
        assertThat(DateFormatter.newInstance().getFormattedDate(timestamp)).isEqualTo("22.01.19")
    }

    @Test
    fun getFormattedDateTime() {
        Locale.setDefault(Locale.US)
        assertThat(DateFormatter.newInstance().getFormattedDateTime(timestamp)).isEqualTo("Tuesday, January 22, 2019 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        assertThat(DateFormatter.newInstance().getFormattedDateTime(timestamp)).isEqualTo("Dienstag, 22. Januar 2019 01:00")
    }

    @Test
    fun getFormattedDateTimeShort() {
        Locale.setDefault(Locale.US)
        assertThat(DateFormatter.newInstance().getFormattedDateTimeShort(timestamp)).isEqualTo("1/22/19 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        assertThat(DateFormatter.newInstance().getFormattedDateTimeShort(timestamp)).isEqualTo("22.01.19 01:00")
    }
}
