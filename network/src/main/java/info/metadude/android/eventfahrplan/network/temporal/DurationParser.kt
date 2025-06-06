package info.metadude.android.eventfahrplan.network.temporal

import info.metadude.android.eventfahrplan.commons.temporal.Duration

/**
 * Parser for the duration string used in the schedule.
 */
object DurationParser {
    @JvmStatic
    fun getMinutes(durationString: String): Duration {
        val parts = durationString.split(':')
        return when (parts.size) {
            1 -> extractMinutesOnly(minutesString = parts[0])
            2 -> extractHoursAndMinutes(hoursString = parts[0], minutesString = parts[1])
            3 -> extractDaysAndHoursAndMinutes(
                daysString = parts[0],
                hoursString = parts[1],
                minutesString = parts[2]
            )

            else -> throw UnknownDurationFormatException(durationString)
        }
    }

    private fun extractMinutesOnly(minutesString: String): Duration {
        return Duration.ofMinutes(minutesString.toLong())
    }

    private fun extractHoursAndMinutes(hoursString: String, minutesString: String): Duration {
        return Duration
            .ofHours(hoursString.toLong())
            .plus(Duration.ofMinutes(minutesString.toLong()))
    }

    // This format was introduced for the CCCamp 2023 schedule. Hopefully support for it can be removed soon.
    // See https://github.com/EventFahrplan/EventFahrplan/pull/561
    private fun extractDaysAndHoursAndMinutes(daysString: String, hoursString: String, minutesString: String): Duration {
        return Duration
            .ofDays(daysString.toLong())
            .plus(Duration.ofHours(hoursString.toLong()))
            .plus(Duration.ofMinutes(minutesString.toLong()))
    }
}

private class UnknownDurationFormatException(durationString: String) : IllegalStateException(
    """Unknown duration format: "$durationString"."""
)
