package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.schedule.TimeSegment.Companion.TIME_GRID_MINIMUM_SEGMENT_HEIGHT
import org.threeten.bp.ZoneOffset

/**
 * Represents a time column segment in the main schedule screen.
 *
 * The given [moment] is rounded down to the nearest grid boundary determined by
 * [TIME_GRID_MINIMUM_SEGMENT_HEIGHT]. The full date and time are retained so matching does
 * not accidentally include the same clock time on another day.
 *
 * The [formatted value][getFormattedText] is rendered in the time column in the main schedule view.
 */
internal class TimeSegment private constructor(moment: Moment) {

    companion object {

        /**
         * Represents both the minimum segment height and 5 minutes.
         */
        const val TIME_GRID_MINIMUM_SEGMENT_HEIGHT = 5

        fun ofMoment(moment: Moment) = TimeSegment(moment)

    }

    /**
     * Moment rounded down to the nearest time grid segment.
     *
     * Examples with a [TIME_GRID_MINIMUM_SEGMENT_HEIGHT] of 5 minutes:
     * - 10:00 remains 10:00.
     * - 10:04 becomes 10:00.
     * - 10:07 becomes 10:05.
     */
    private val roundedMoment: Moment

    init {
        val remainder = moment.minuteOfDay % TIME_GRID_MINIMUM_SEGMENT_HEIGHT
        roundedMoment = moment.minusMinutes(remainder.toLong())
    }

    /**
     * Returns the normalized and formatted text representing the given minutes of the day.
     * This text is ready to be displayed in the time column.
     *
     * See [DateFormatter.getFormattedTime24Hour].
     */
    fun getFormattedText(sessionZoneOffset: ZoneOffset?, useDeviceTimeZone: Boolean): String =
        DateFormatter.newInstance(useDeviceTimeZone).getFormattedTime24Hour(roundedMoment, sessionZoneOffset)

    /**
     * Returns true if the given [otherMoment] is inside the half-open interval
     * `[roundedMoment, roundedMoment + minutesOffset)`. Otherwise, false.
     * Consequently, a [minutesOffset] of 0 spans an empty interval which never matches.
     * An [IllegalArgumentException] is thrown if [minutesOffset] is negative.
     */
    fun isMatched(otherMoment: Moment, minutesOffset: Int): Boolean {
        require(minutesOffset >= 0) { "Minutes offset is $minutesOffset but must be 0 or more." }
        if (otherMoment.isBefore(roundedMoment)) {
            return false
        }
        return otherMoment.isBefore(roundedMoment.plusMinutes(minutesOffset.toLong()))
    }

    override fun toString() = roundedMoment.toString()

}
