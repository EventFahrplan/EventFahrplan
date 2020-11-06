package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Format timestamps according to system locale and system time zone.
 */
class DateFormatter private constructor() {
    private val timeShort = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
    private val timeShortNumberOnly = DateTimeFormatter.ofPattern("HH:mm")
    private val dateShort = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
    private val dateTimeShort = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    private val dateFullTimeShortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
    private val timeZoneOffsetFormatter = DateTimeFormatter.ofPattern("z")

    /**
     * Returns 01:00, 14:00 etc. for all locales. Always without AM or PM postfix in 24 hour format.
     */
    fun getFormattedTime24Hour(moment: Moment): String =
            timeShortNumberOnly.format(moment.toZonedDateTime(OffsetDateTime.now().offset))

    /**
     * Returns 01:00 AM, 02:00 PM, 14:00 etc, depending on current system locale either
     * in 24 or 12 hour format. The latter featuring AM or PM postfixes.
     */
    fun getFormattedTime(time: Long): String = timeShort.format(Date(time))

    /**
     * Returns day month and year in current system locale.
     * E.g. 1/22/19 or 22.01.19
     */
    fun getFormattedDate(time: Long): String = dateShort.format(Date(time))

    /**
     * Returns a formatted string suitable for sharing it with people worldwide.
     * It contains day, month, year and time in current system locale in long format
     * and the associated time zone offset.
     * E.g. Tuesday, January 22, 2019 1:00 AM GMT+01:00
     */
    fun getFormattedShareable(time: Long): String {
        // TODO: Use time zone of the event rather than the device time zone.
        val displayTimeZone = ZoneId.systemDefault()
        val sessionStartTime = Instant.ofEpochMilli(time)
        val timeZoneOffset = timeZoneOffsetFormatter.withZone(displayTimeZone).format(sessionStartTime)
        // TODO Append time zone name once it is provided. See https://github.com/EventFahrplan/EventFahrplan/pull/296.
        val sessionDateTime = dateFullTimeShortFormatter.withZone(displayTimeZone).format(sessionStartTime)
        return "$sessionDateTime $timeZoneOffset"
    }

    /**
     * Returns day month, year and time in current system locale in short format.
     * E.g. 1/22/19 1:00 AM
     */
    fun getFormattedDateTimeShort(time: Long): String = dateTimeShort.format(Date(time))

    companion object {

        @JvmStatic
        fun newInstance(): DateFormatter {
            return DateFormatter()
        }
    }
}
