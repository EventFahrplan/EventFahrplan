package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * Represents a number of days. Starting at [startDay] 00:00 and ending at [endDay] 23:59:59.
 * Pass only [startDay] if your range spans only a single day.
 * */
data class DayRange(
        val startDay: Moment,
        val endDay: Moment = startDay
) {
    val startsAt: ZonedDateTime = startDay.startOfDay().toZonedDateTime(ZoneOffset.UTC)
    val endsAt: ZonedDateTime = endDay.endOfDay().toZonedDateTime(ZoneOffset.UTC)

    /**
     * Check if given [dateTime] is within range.
     * @return true if and only if given [dateTime] is within [startsAt] and [endsAt] (inclusive)
     */
    fun contains(dateTime: ZonedDateTime) =
            this.startsAt <= dateTime && dateTime <= this.endsAt

}
