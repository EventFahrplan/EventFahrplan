package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.schedule.TimeSegment.Companion.TIME_GRID_MINIMUM_SEGMENT_HEIGHT

/**
 * Holds the minutes of the day which represent a time segment (hours and minutes).
 * Day, month and year are not considered here.
 *
 * The value is rendered in the time column in the main schedule view.
 *
 * The given moment is normalized. The minutes of the day value is rounded to fit
 * into the time grid determined by [TIME_GRID_MINIMUM_SEGMENT_HEIGHT].
 */
internal class TimeSegment private constructor(

        moment: Moment

) {

    companion object {

        /**
         * Represents both the minimum segment height and 5 minutes.
         */
        const val TIME_GRID_MINIMUM_SEGMENT_HEIGHT = 5

        @Deprecated("Use TimeSegment.ofMoment", replaceWith = ReplaceWith("TimeSegment.ofMoment()"))
        @JvmStatic
        fun ofMinutesOfTheDay(minutesOfTheDay: Int): TimeSegment {
            return ofMoment(Moment.now().startOfDay().plusMinutes(minutesOfTheDay.toLong()))
        }

        @JvmStatic
        fun ofMoment(moment: Moment) = TimeSegment(moment)

    }

    private val roundedMoment: Moment

    init {
        val remainder = moment.minuteOfDay % TIME_GRID_MINIMUM_SEGMENT_HEIGHT
        roundedMoment = moment.minusMinutes(remainder.toLong())
    }

    /**
     * Returns the normalized and formatted text representing the given minutes of the day.
     * This text is ready to be displayed in the time column.
     */
    val formattedText: String
        get() = DateFormatter.newInstance().getFormattedTime24Hour(roundedMoment)

    /**
     * Returns true if the given [otherMoment] matches the internal rounded moment taken the
     * [minutesOffset] into account. Otherwise false.
     */
    fun isMatched(otherMoment: Moment, minutesOffset: Int) =
            otherMoment.hour == roundedMoment.hour &&
                    otherMoment.minute >= roundedMoment.minute &&
                    otherMoment.minute < roundedMoment.minute + minutesOffset

}
