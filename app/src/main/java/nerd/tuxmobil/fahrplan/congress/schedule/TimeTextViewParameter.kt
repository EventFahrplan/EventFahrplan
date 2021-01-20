package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.annotation.LayoutRes
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.BOX_HEIGHT_MULTIPLIER
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.FIFTEEN_MINUTES
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.ONE_DAY

/**
 * Parameters to be used to inflate and configure a time text view.
 */
@Suppress("DataClassPrivateConstructor")
internal data class TimeTextViewParameter private constructor(

        @LayoutRes
        val layout: Int,
        val height: Int,
        val titleText: String

) {

    companion object {

        /**
         * Returns a list of parameters to be used to inflate and configure a time text view.
         */
        @JvmStatic
        fun parametersOf(
                nowMoment: Moment,
                conference: Conference,
                firstDayStartDay: Int,
                dayIndex: Int,
                normalizedBoxHeight: Int
        ): List<TimeTextViewParameter> {
            val parameters = mutableListOf<TimeTextViewParameter>()
            var time = conference.firstSessionStartsAt
            var printTime = time
            val timeTextViewHeight = BOX_HEIGHT_MULTIPLIER * normalizedBoxHeight
            var timeSegment: TimeSegment
            while (time < conference.lastSessionEndsAt) {
                timeSegment = TimeSegment.ofMinutesOfTheDay(printTime)
                var timeTextLayout: Int
                val isToday = nowMoment.monthDay - firstDayStartDay == dayIndex - 1
                timeTextLayout = if (isToday && timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                    R.layout.time_layout_now
                } else {
                    R.layout.time_layout
                }
                val parameter = TimeTextViewParameter(timeTextLayout, timeTextViewHeight, timeSegment.formattedText)
                parameters.add(parameter)
                time += FIFTEEN_MINUTES
                printTime = time
                if (printTime >= ONE_DAY) {
                    printTime -= ONE_DAY
                }
            }
            return parameters
        }
    }

}
