package nerd.tuxmobil.fahrplan.congress.schedule.observables

import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.schedule.Conference
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.BOX_HEIGHT_MULTIPLIER
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.FIFTEEN_MINUTES
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel
import nerd.tuxmobil.fahrplan.congress.schedule.TimeSegment

/**
 * Payload of the observable [timeTextViewParameters][FahrplanViewModel.timeTextViewParameters]
 * property in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 * Parameters to be used to inflate and configure a time text view.
 */
@Suppress("DataClassPrivateConstructor")
internal data class TimeTextViewParameter @VisibleForTesting constructor(

        @LayoutRes
        val layout: Int,
        val height: Int,
        val titleText: String

) {

    companion object {

        /**
         * Returns a list of parameters to be used to inflate and configure a time text view.
         */
        fun parametersOf(
                nowMoment: Moment,
                conference: Conference,
                firstDayStartDay: Int,
                dayIndex: Int,
                normalizedBoxHeight: Int,
                useDeviceTimeZone: Boolean
        ): List<TimeTextViewParameter> {
            val parameters = mutableListOf<TimeTextViewParameter>()
            var sessionStartsAt = conference.firstSessionStartsAt
            var sessionStartsAtMinutes = sessionStartsAt.minuteOfDay
            val timeTextViewHeight = BOX_HEIGHT_MULTIPLIER * normalizedBoxHeight
            var timeSegment: TimeSegment
            val minutesToAdd = if (conference.spansMultipleDays) MINUTES_OF_ONE_DAY else 0
            val lastSessionEndsAtMinutes = conference.lastSessionEndsAt.minuteOfDay + minutesToAdd
            while (sessionStartsAtMinutes < lastSessionEndsAtMinutes) {
                timeSegment = TimeSegment.ofMoment(sessionStartsAt)
                val isToday = nowMoment.monthDay - firstDayStartDay == dayIndex - 1
                val timeTextLayout = if (isToday && timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                    R.layout.time_layout_now
                } else {
                    R.layout.time_layout
                }
                val titleText = timeSegment.getFormattedText(conference.timeZoneOffset, useDeviceTimeZone)
                val parameter = TimeTextViewParameter(timeTextLayout, timeTextViewHeight, titleText)
                parameters.add(parameter)
                sessionStartsAt = sessionStartsAt.plusMinutes(FIFTEEN_MINUTES.toLong())
                sessionStartsAtMinutes += FIFTEEN_MINUTES
            }
            return parameters
        }
    }

}
