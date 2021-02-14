package info.metadude.android.eventfahrplan.commons.temporal

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

object DateParser {

    /**
     * Parses the given [text] and returns its time zone offset value represented in seconds.
     *
     * The [text] is expected to be provided in the [ISO_DATE_TIME][DateTimeFormatter.ISO_DATE_TIME]
     * format (e.g. 2019-01-01T00:00:00Z).
     */
    @JvmStatic
    fun parseTimeZoneOffset(text: String): Int {
        val zonedDateTime = try {
            ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Error parsing time zone offset from: '$text'.")
        }
        return zonedDateTime.offset.totalSeconds
    }

}
