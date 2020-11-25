package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import info.metadude.android.eventfahrplan.commons.temporal.Moment

/**
 * Holds the minutes of the day which represent a time segment (hours and minutes).
 * Day, month and year are not considered here.
 *
 * The value is rendered in the time column in the main schedule view.
 *
 * The given [minutesOfTheDay] are normalized. The minutes value is rounded to fit
 * into the time grid determined by [TIME_GRID_MINIMUM_SEGMENT_HEIGHT].
 */
internal class TimeSegment private constructor(

        private val minutesOfTheDay: Int

) {

    companion object {

        /**
         * Represents both the minimum segment height and 5 minutes.
         */
        const val TIME_GRID_MINIMUM_SEGMENT_HEIGHT = 5

        @JvmStatic
        fun ofMinutesOfTheDay(minutesOfTheDay: Int) = TimeSegment(minutesOfTheDay)

    }

    private val moment: Moment
        get() {
            val remainder = minutesOfTheDay % TIME_GRID_MINIMUM_SEGMENT_HEIGHT
            return Moment.now().startOfDay().plusMinutes((minutesOfTheDay - remainder).toLong())
        }

    /**
     * Returns the normalized and formatted text representing the given minutes of the day.
     * This text is ready to be displayed in the time column.
     */
    val formattedText: String
        get() = DateFormatter.newInstance().getFormattedTime24Hour(moment)

    fun isMatched(otherMoment: Moment, offset: Int) =
            otherMoment.hour == moment.hour &&
                    otherMoment.minute >= moment.minute &&
                    otherMoment.minute < moment.minute + offset

}
