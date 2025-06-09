package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/**
 * Format timestamps according to system locale and system time zone.
 */
class DateFormatter private constructor(
    private val zoneOffsetProvider: ZoneOffsetProvider,
) {

    private val timeShortNumberOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timeShortFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private val dateShortFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    private val dateShortTimeShortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
    private val dateLongTimeShortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
    private val dateFullTimeShortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
    private val timeZoneOffsetFormatter = DateTimeFormatter.ofPattern("z")

    /**
     * Returns a formatted `hours:minutes` string. Formatting happens by taking the [original time
     * zone of the associated session][sessionZoneOffset] into account. If [sessionZoneOffset] is
     * missing then formatting falls back to using the current time zone offset of the device.
     *
     * The returned text is formatted equally for all locales, e.g. 01:00, 14:00 etc. - always
     * without AM or PM postfix - in 24 hours format.
     */
    fun getFormattedTime24Hour(moment: Moment, sessionZoneOffset: ZoneOffset?): String {
        val zoneOffset = zoneOffsetProvider.getAvailableZoneOffset(sessionZoneOffset)
        return timeShortNumberOnlyFormatter.format(moment.toZonedDateTime(zoneOffset))
    }

    /**
     * Returns 01:00 AM, 02:00 PM, 14:00 etc, depending on current system locale either
     * in 24 or 12 hour format. The latter featuring AM or PM postfixes.
     *
     * Formatting happens by taking the [original time zone of the associated session][sessionZoneOffset]
     * into account. If [sessionZoneOffset] is missing then formatting falls back to using the
     * current time zone offset of the device.
     */
    fun getFormattedTime(time: Long, sessionZoneOffset: ZoneOffset?): String {
        val zoneOffset = zoneOffsetProvider.getAvailableZoneOffset(sessionZoneOffset)
        return timeShortFormatter.withZone(zoneOffset).format(Instant.ofEpochMilli(time))
    }

    /**
     * Returns day, month and year in current system locale.
     * E.g. 1/22/19 or 22.01.19
     *
     * Formatting happens by taking the [original time zone of the associated session][sessionZoneOffset]
     * into account. If [sessionZoneOffset] is missing then formatting falls back to using the
     * current time zone offset of the device.
     */
    fun getFormattedDate(time: Long, sessionZoneOffset: ZoneOffset?): String {
        val zoneOffset = zoneOffsetProvider.getAvailableZoneOffset(sessionZoneOffset)
        return dateShortFormatter.withZone(zoneOffset).format(Instant.ofEpochMilli(time))
    }

    /**
     * Returns a formatted string suitable for sharing it with people worldwide.
     * It consists of day, month, year, time, time zone offset in the time zone of the event or
     * in current system time zone if the former is not provided.
     *
     * The human readable name '{area}/{city}' of the time zone ID is appended if available.
     *
     * Formatting example:
     * Tuesday, January 22, 2019, 1:00 AM GMT+01:00 (Europe/Berlin)
     */
    fun getFormattedShareable(time: Long, timeZoneId: ZoneId?): String {
        val displayTimeZone = timeZoneId ?: ZoneId.systemDefault()
        val sessionStartTime = Instant.ofEpochMilli(time)
        val timeZoneOffset = timeZoneOffsetFormatter.withZone(displayTimeZone).format(sessionStartTime)
        val sessionDateTime = dateFullTimeShortFormatter.withZone(displayTimeZone).format(sessionStartTime)
        var shareableText = "$sessionDateTime $timeZoneOffset"
        if (timeZoneId != null) {
            shareableText += " (${displayTimeZone.id})"
        }
        return shareableText
    }

    /**
     * Returns day, month, year and time in short format. Formatting happens by taking the [original
     * time zone of the associated session][sessionZoneOffset] into account. If [sessionZoneOffset]
     * is missing then formatting falls back to using the current time zone offset of the device.
     *
     * E.g. 1/22/19, 1:00 AM
     */
    fun getFormattedDateTimeShort(time: Long, sessionZoneOffset: ZoneOffset?): String {
        val zoneOffset = zoneOffsetProvider.getAvailableZoneOffset(sessionZoneOffset)
        val toZonedDateTime: ZonedDateTime = Moment.ofEpochMilli(time).toZonedDateTime(zoneOffset)
        return dateShortTimeShortFormatter.format(toZonedDateTime)
    }

    /**
     * Returns day, month, year and time in long format. Formatting happens by taking the [original
     * time zone of the associated session][sessionZoneOffset] into account. If [sessionZoneOffset]
     * is missing then formatting falls back to using the current time zone offset of the device.
     *
     * E.g. January 22, 2019, 1:00 AM
     */
    fun getFormattedDateTimeLong(time: Long, sessionZoneOffset: ZoneOffset?): String {
        val zoneOffset = zoneOffsetProvider.getAvailableZoneOffset(sessionZoneOffset)
        val toZonedDateTime = Moment.ofEpochMilli(time).toZonedDateTime(zoneOffset)
        return dateLongTimeShortFormatter.format(toZonedDateTime)
    }

    companion object {

        fun newInstance(useDeviceTimeZone: Boolean): DateFormatter {
            val zoneOffsetProvider = ZoneOffsetProvider(Clock.systemDefaultZone(), useDeviceTimeZone)
            return DateFormatter(zoneOffsetProvider)
        }
    }
}
