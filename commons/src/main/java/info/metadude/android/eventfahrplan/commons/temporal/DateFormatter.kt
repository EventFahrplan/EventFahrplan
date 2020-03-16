package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
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
     * Returns day month, year and time in current system locale in long format.
     * E.g. Tuesday, January 22, 2019 1:00 AM
     */
    fun getFormattedDateTime(time: Long): String = dateTimeFull.format(Date(time))

    /**
     * Returns day month, year and time in current system locale in short format.
     * E.g. 1/22/19 1:00 AM
     */
    fun getFormattedDateTimeShort(time: Long): String = dateTimeShort.format(Date(time))

    companion object {

        @JvmStatic
        fun newInstance(): DateFormatter {
            return DateFormatter();
        }
    }
}
