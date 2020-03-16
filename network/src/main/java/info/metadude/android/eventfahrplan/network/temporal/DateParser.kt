package info.metadude.android.eventfahrplan.network.temporal

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

class DateParser {

    companion object {

        /**
         * Parses given [text] and returns its date value represented in milliseconds.
         *
         * @param text either ISO-8601 date and time format (e.g. 2019-01-01T00:00:00Z)
         * or ISO-8601 date format (i.e. 2019-01-01).
         */
        @JvmStatic
        fun getDateTime(text: String): Long {
            if (text.length > 10) {
                val parsed = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(text))
                val atUTCOffset = parsed.atOffset(ZoneOffset.UTC)
                return atUTCOffset.toEpochSecond() * 1000
            } else {
                val parsed = LocalDate.parse(text)
                val atUTCOffset = parsed.atTime(0, 0).atOffset(ZoneOffset.UTC)
                return atUTCOffset.toEpochSecond() * 1000
            }
        }

        /**
         * Returns [Moment.minuteOfDay] of given parse [text].
         *
         * @param text see [DateParser.getDateTime] for valid formats
         */
        @JvmStatic
        fun getDayChange(text: String): Int {
            val timeUTC = getDateTime(text)
            return Moment(timeUTC).minuteOfDay
        }
    }
}
