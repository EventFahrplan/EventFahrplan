package info.metadude.android.eventfahrplan.commons.temporal

import androidx.annotation.VisibleForTesting
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

object DateParser {

    /**
     * Parses the given [text] and returns its date value represented in milliseconds.
     *
     * Expects the [text] to be provided in [ISO_DATE_TIME][DateTimeFormatter.ISO_DATE_TIME]
     * format (e.g. 2019-01-01T00:00:00Z).
     */
    fun parseDateTime(text: String): Long {
        val instant = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(text))
        val atUtcOffset = instant.atOffset(ZoneOffset.UTC)
        return atUtcOffset.toEpochSecond() * Moment.MILLISECONDS_OF_ONE_SECOND
    }

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
        } catch (_: DateTimeParseException) {
            throw TimeZoneOffsetParsingException(text)
        }
        return zonedDateTime.offset.totalSeconds
    }

}

@VisibleForTesting
internal class TimeZoneOffsetParsingException(text: String) : IllegalArgumentException(
    """Error parsing time zone offset from: "$text"."""
)
