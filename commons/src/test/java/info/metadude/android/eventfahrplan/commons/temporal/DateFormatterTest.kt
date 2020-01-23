package info.metadude.android.eventfahrplan.commons.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class DateFormatterTest {
    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()
    private val timestamp = Moment("2019-01-22").toMilliseconds()

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
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedTime(timestamp)).isEqualTo("1:00 AM")

        Locale.setDefault(Locale("de", "DE"))
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedTime(timestamp)).isEqualTo("01:00")

        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"))
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedTime(timestamp)).isEqualTo("06:00")
    }

    @Test
    fun getFormattedDate() {
        Locale.setDefault(Locale.US)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDate(timestamp)).isEqualTo("1/22/19")

        Locale.setDefault(Locale.GERMANY)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDate(timestamp)).isEqualTo("22.01.19")
    }

    @Test
    fun getFormattedDateTime() {
        Locale.setDefault(Locale.US)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDateTime(timestamp)).isEqualTo("Tuesday, January 22, 2019 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDateTime(timestamp)).isEqualTo("Dienstag, 22. Januar 2019 01:00")
    }

    @Test
    fun getFormattedDateTimeShort() {
        Locale.setDefault(Locale.US)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDateTimeShort(timestamp)).isEqualTo("1/22/19 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        DateFormatter.initialize()
        assertThat(DateFormatter.getFormattedDateTimeShort(timestamp)).isEqualTo("22.01.19 01:00")
    }
}
