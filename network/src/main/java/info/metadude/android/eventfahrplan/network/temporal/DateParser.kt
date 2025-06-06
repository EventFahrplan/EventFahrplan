package info.metadude.android.eventfahrplan.network.temporal

import info.metadude.android.eventfahrplan.commons.temporal.DateParser
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_SECOND
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoField

class DateParser {

    companion object {

        /**
         * Parses given [text] and returns its date value represented in milliseconds.
         *
         * @param text either ISO-8601 date and time format (e.g. 2019-01-01T00:00:00Z)
         * or ISO-8601 date format (i.e. 2019-01-01).
         *
         * Also see [DateParser.parseDateTime].
         */
        @JvmStatic
        fun getDateTime(text: String) = if (text.length > 10) {
            DateParser.parseDateTime(text)
        } else {
            val parsed = LocalDate.parse(text)
            val atUTCOffset = parsed.atTime(0, 0).atOffset(ZoneOffset.UTC)
            atUTCOffset.toEpochSecond() * MILLISECONDS_OF_ONE_SECOND
        }

        /**
         * Returns [Moment.minuteOfDay] of given parse [text].
         *
         * @param text see [getDateTime] for valid formats
         */
        @JvmStatic
        fun getDayChange(text: String): Int {
            val timeUTC = getDateTime(text)
            return Moment.ofEpochMilli(timeUTC).minuteOfDay
        }

        /**
         * Returns an integer value representing the minutes of the given [hoursMinutes] text.
         * Expected format of the [hoursMinutes] text is hh:mm.
         *
         * Examples:
         * - 00:30 -> 30
         * -  1:30 -> 90
         * - 23:59 -> 1439
         */
        @JvmStatic
        fun getMinutes(hoursMinutes: String): Duration {
            val minutes = DateTimeFormatter
                .ofPattern("H:mm[:ss]")
                .parse(hoursMinutes)
                .get(ChronoField.MINUTE_OF_DAY)
            return Duration.ofMinutes(minutes)
        }

    }
}
