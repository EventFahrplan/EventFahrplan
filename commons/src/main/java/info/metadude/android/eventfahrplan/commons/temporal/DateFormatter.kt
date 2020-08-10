package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * Format timestamps according to system locale and system time zone.
 */
class DateFormatter private constructor() {
    private val timeShort = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
    private val timeShortNumberOnly = DateTimeFormatter.ofPattern("HH:mm")
    private val dateShort = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
    private val dateTimeShort = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    private val dateTimeFull = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT)

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
        val timeZoneOffset = dateTimeFull.timeZone.getDisplayName(true, TimeZone.SHORT)
        // TODO Append time zone name once it is provided. See https://github.com/EventFahrplan/EventFahrplan/pull/296.
        return "${dateTimeFull.format(Date(time))} $timeZoneOffset"
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
