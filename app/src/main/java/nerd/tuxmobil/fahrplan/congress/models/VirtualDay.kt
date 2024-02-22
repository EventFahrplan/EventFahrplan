package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.schedule.Conference
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame

/**
 * Represents a "conference day" that is not bound to a specific date. Sessions can take place on
 * different days but still be grouped together in a virtual day. It does not have typical boundaries
 * like a "natural day" (00:00:00 to 23:59:59) has. The [timeRange] property returns the time range
 * of all sessions spanning from the start of the earliest session to the end of the latest session.
 *
 * Similar: [Conference], [ConferenceTimeFrame].
 */
data class VirtualDay(
    val index: Int,
    val sessions: List<Session>,
) {
    init {
        require(index > 0) { "Index must be greater than zero." }
        require(sessions.isNotEmpty()) { "Sessions must not be empty." }
    }

    val timeFrame: ClosedRange<Moment>
        get() {
            val startsAtSorted = sessions.sortedBy { it.startsAt.toMilliseconds() }
            val endsAtSorted = sessions.sortedBy { it.endsAt.toMilliseconds() }

            val earliestMoment = startsAtSorted.first().startsAt
            val latestMoment = endsAtSorted.last().endsAt

            return earliestMoment..latestMoment
        }

    override fun toString(): String {
        return "VirtualDay(index=$index, timeFrame=$timeFrame, sessions=${sessions.size})"
    }
}
