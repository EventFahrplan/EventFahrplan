package info.metadude.android.eventfahrplan.commons.temporal

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.util.Locale
import java.util.TimeZone

class DateFormatterTest {

    private companion object {
        val NO_TIME_ZONE_ID: ZoneId? = null
        val TIME_ZONE_EUROPE_BERLIN: ZoneId = ZoneId.of("Europe/Berlin")
    }

    private val systemTimezone = TimeZone.getDefault()
    private val systemLocale = Locale.getDefault()
    private val moment = Moment.parseDate("2019-01-22")

    @BeforeEach
    fun resetTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
    }

    @AfterEach
    fun resetSystemDefaults() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun getFormattedTime() {
        Locale.setDefault(Locale("en", "US"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
        assertThat(createDateFormatter().getFormattedTime(moment, getTimeZoneOffsetNow())).isEqualTo("1:00 AM")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(createDateFormatter().getFormattedTime(moment, getTimeZoneOffsetNow())).isEqualTo("2:00 PM")

        Locale.setDefault(Locale("de", "DE"))
        assertThat(createDateFormatter().getFormattedTime(moment, getTimeZoneOffsetNow())).isEqualTo("14:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"))
        assertThat(createDateFormatter().getFormattedTime(moment, getTimeZoneOffsetNow())).isEqualTo("06:00")
    }

    @Test
    fun getFormattedTimeNumbersOnly() {
        Locale.setDefault(Locale("en", "US"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
        assertThat(createDateFormatter().getFormattedTime24Hour(moment, getTimeZoneOffsetNow())).isEqualTo("01:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(createDateFormatter().getFormattedTime24Hour(moment, getTimeZoneOffsetNow())).isEqualTo("14:00")

        Locale.setDefault(Locale("de", "DE"))
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+6"))
        assertThat(createDateFormatter().getFormattedTime24Hour(moment, getTimeZoneOffsetNow())).isEqualTo("06:00")

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+14"))
        assertThat(createDateFormatter().getFormattedTime24Hour(moment, getTimeZoneOffsetNow())).isEqualTo("14:00")
    }

    @Test
    fun getFormattedDate() {
        Locale.setDefault(Locale.US)
        assertThat(createDateFormatter().getFormattedDate(moment, getTimeZoneOffsetNow())).isEqualTo("1/22/19")

        Locale.setDefault(Locale.GERMANY)
        assertThat(createDateFormatter().getFormattedDate(moment, getTimeZoneOffsetNow())).isEqualTo("22.01.19")
    }

    // This test only passes when being executed in a JDK 9+ environment.
    // See https://stackoverflow.com/questions/65732319/how-to-stabilize-flaky-datetimeformatteroflocalizeddatetime-test
    @Test
    fun getFormattedShareable() {
        Locale.setDefault(Locale.US)
        assertThat(createDateFormatter().getFormattedShareable(moment, NO_TIME_ZONE_ID))
                .isEqualTo("Tuesday, January 22, 2019, 1:00 AM GMT+01:00")

        Locale.setDefault(Locale.GERMANY)
        assertThat(createDateFormatter().getFormattedShareable(moment, NO_TIME_ZONE_ID))
                .isEqualTo("Dienstag, 22. Januar 2019, 01:00 GMT+01:00")

        Locale.setDefault(Locale.US)
        assertThat(createDateFormatter().getFormattedShareable(moment, TIME_ZONE_EUROPE_BERLIN))
                .isEqualTo("Tuesday, January 22, 2019, 1:00 AM CET (Europe/Berlin)")

        Locale.setDefault(Locale.GERMANY)
        assertThat(createDateFormatter().getFormattedShareable(moment, TIME_ZONE_EUROPE_BERLIN))
                .isEqualTo("Dienstag, 22. Januar 2019, 01:00 MEZ (Europe/Berlin)")
    }

    // This test only passes when being executed in a JDK 9+ environment.
    // See https://stackoverflow.com/questions/65732319/how-to-stabilize-flaky-datetimeformatteroflocalizeddatetime-test
    @Test
    fun getFormattedDateTimeShort() {
        Locale.setDefault(Locale.US)
        assertThat(createDateFormatter().getFormattedDateTimeShort(moment, getTimeZoneOffsetNow())).isEqualTo("1/22/19, 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        assertThat(createDateFormatter().getFormattedDateTimeShort(moment, getTimeZoneOffsetNow())).isEqualTo("22.01.19, 01:00")
    }

    // This test only passes when being executed in a JDK 9+ environment.
    // See https://stackoverflow.com/questions/65732319/how-to-stabilize-flaky-datetimeformatteroflocalizeddatetime-test
    @Test
    fun getFormattedDateTimeLong() {
        Locale.setDefault(Locale.US)
        assertThat(createDateFormatter().getFormattedDateTimeLong(moment, getTimeZoneOffsetNow())).isEqualTo("January 22, 2019, 1:00 AM")

        Locale.setDefault(Locale.GERMANY)
        assertThat(createDateFormatter().getFormattedDateTimeLong(moment, getTimeZoneOffsetNow())).isEqualTo("22. Januar 2019, 01:00")
    }

    private fun createDateFormatter(): DateFormatter {
        return DateFormatter.newInstance(useDeviceTimeZone = false)
    }

    private fun getTimeZoneOffsetNow(): ZoneOffset {
        return OffsetDateTime.now().offset
    }

}
