package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session

data class Conference(

        var firstSessionStartsAt: Int = 0,
        var lastSessionEndsAt: Int = 0

) {

    /**
     * Calculates the [firstSessionStartsAt] and [lastSessionEndsAt] time stamps for the
     * given sorted sessions.
     *
     * This methods contains specific handling for Frab and Pentabarf schedule data.
     * 09/2018: The latter can probably be dropped since unmodified Pentabarf schedule
     * data has not been consumed by the app(s) for years.
     *
     * @param sessions     Sorted list of sessions.
     * @param minutesOfDay Function to calculate the minutes of the day for the
     *                     given UTC time stamp.
     */
    fun calculateTimeFrame(sessions: List<Session>, minutesOfDay: (dateUtc: Long) -> Int) {
        val firstSession = sessions[0] // they are already sorted
        var end: Long = 0
        val firstSessionDateUtc = firstSession.dateUTC
        firstSessionStartsAt = if (firstSessionDateUtc > 0) {
            // Frab
            minutesOfDay(firstSessionDateUtc)
        } else {
            // Pentabarf
            firstSession.relStartTime
        }
        lastSessionEndsAt = -1
        for (session in sessions) {
            if (firstSessionDateUtc > 0) {
                // Frab
                val sessionEndsAt = session.endsAtDateUtc
                if (end == 0L) {
                    end = sessionEndsAt
                } else if (sessionEndsAt > end) {
                    end = sessionEndsAt
                }
            } else {
                // Pentabarf
                val sessionEndsAt = session.relStartTime + session.duration
                if (lastSessionEndsAt == -1) {
                    lastSessionEndsAt = sessionEndsAt
                } else if (sessionEndsAt > lastSessionEndsAt) {
                    lastSessionEndsAt = sessionEndsAt
                }
            }
        }
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
