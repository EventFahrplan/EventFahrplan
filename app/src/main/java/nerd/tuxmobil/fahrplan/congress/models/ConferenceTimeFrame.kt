package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater
import nerd.tuxmobil.fahrplan.congress.schedule.Conference

/**
 * Represents the time frame of a conference which is taken into account by the [AlarmUpdater]
 * to calculate if and when alarms should fire.
 *
 * These classes should not be used for other purposes.
 * They are not a replacement for the [Conference] class.
 */
sealed interface ConferenceTimeFrame {

    data object Unknown : ConferenceTimeFrame
    data class Known(val firstDayStartTime: Moment, val lastDayEndTime: Moment) : ConferenceTimeFrame {

        private val timeFrame = firstDayStartTime..lastDayEndTime

        init {
            check(isValid) { "Invalid conference time frame: $this" }
        }

        val isValid: Boolean
            get() = timeFrame.start.isBefore(timeFrame.endInclusive)

        operator fun contains(moment: Moment) =
            startsAtOrBefore(moment) && timeFrame.endInclusive.isAfter(moment)

        fun endsAtOrBefore(moment: Moment) =
            timeFrame.endInclusive.isSimultaneousWith(moment) || timeFrame.endInclusive.isBefore(moment)

        fun startsAfter(moment: Moment) =
            timeFrame.start.isAfter(moment)

        fun startsAtOrBefore(moment: Moment) =
            timeFrame.start.isSimultaneousWith(moment) || timeFrame.start.isBefore(moment)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Known
            return timeFrame == other.timeFrame
        }

        override fun hashCode() = timeFrame.hashCode()

        override fun toString() =
            timeFrame.toString()

    }
}
