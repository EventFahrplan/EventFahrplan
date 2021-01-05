package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session

// TODO Use Moment class, merge with ConferenceTimeFrame class?
data class Conference(

        var firstSessionStartsAt: Int = 0,
        var lastSessionEndsAt: Int = 0

) {

    /**
     * Calculates the [firstSessionStartsAt] and [lastSessionEndsAt] time stamps for the
     * given sorted sessions.
     *
     * @param sessions     Sorted list of sessions.
     * @param minutesOfDay Function to calculate the minutes of the day for the
     *                     given UTC time stamp.
     */
    fun calculateTimeFrame(sessions: List<Session>, minutesOfDay: (dateUtc: Long) -> Int) {
        val firstSession = sessions[0] // they are already sorted
        var end: Long = 0
        val firstSessionDateUtc = firstSession.dateUTC
        if (firstSessionDateUtc > 0) {
            firstSessionStartsAt = minutesOfDay(firstSessionDateUtc)
            for (session in sessions) {
                val sessionEndsAt = session.endsAtDateUtc
                if (end == 0L) {
                    end = sessionEndsAt
                } else if (sessionEndsAt > end) {
                    end = sessionEndsAt
                }
            }
        }
        lastSessionEndsAt = -1
        if (end > 0) {
            lastSessionEndsAt = minutesOfDay(end)
            if (isDaySwitch(firstSessionDateUtc, end)) {
                forwardLastSessionEndsAtByOneDay()
            }
        }
    }

    private fun isDaySwitch(startUtc: Long, endUtc: Long): Boolean {
        val startDay = Moment.ofEpochMilli(startUtc).monthDay
        val endDay = Moment.ofEpochMilli(endUtc).monthDay
        return startDay != endDay
    }

    private fun forwardLastSessionEndsAtByOneDay() {
        lastSessionEndsAt += MINUTES_OF_ONE_DAY
    }

}
