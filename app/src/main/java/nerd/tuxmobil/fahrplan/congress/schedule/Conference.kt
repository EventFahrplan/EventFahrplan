package nerd.tuxmobil.fahrplan.congress.schedule

import android.support.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

data class Conference(

        var firstEventStartsAt: Int = 0,
        var lastEventEndsAt: Int = 0

) {

    /**
     * Calculates the [firstEventStartsAt] and [lastEventEndsAt] time stamps for the
     * given sorted events.
     *
     * This methods contains specific handling for Frab and Pentabarf schedule data.
     * 09/2018: The latter can probably be dropped since unmodified Pentabarf schedule
     * data has not been consumed by the app(s) for years.
     *
     * @param events       Sorted list of events.
     * @param minutesOfDay Function to calculate the minutes of the day for the
     *                     given UTC time stamp.
     */
    fun calculateTimeFrame(events: List<Event>, minutesOfDay: (dateUtc: Long) -> Int) {
        val firstEvent = events[0] // they are already sorted
        var end: Long = 0
        val firstEventDateUtc = firstEvent.dateUTC
        firstEventStartsAt = if (firstEventDateUtc > 0) {
            // Frab
            minutesOfDay(firstEventDateUtc)
        } else {
            // Pentabarf
            firstEvent.relStartTime
        }
        lastEventEndsAt = -1
        for (event in events) {
            if (firstEventDateUtc > 0) {
                // Frab
                val eventEndsAt = event.dateUTC + event.duration * 60000
                if (end == 0L) {
                    end = eventEndsAt
                } else if (eventEndsAt > end) {
                    end = eventEndsAt
                }
            } else {
                // Pentabarf
                val eventEndsAt = event.relStartTime + event.duration
                if (lastEventEndsAt == -1) {
                    lastEventEndsAt = eventEndsAt
                } else if (eventEndsAt > lastEventEndsAt) {
                    lastEventEndsAt = eventEndsAt
                }
            }
        }
        if (end > 0) {
            lastEventEndsAt = minutesOfDay(end)
            if (isDaySwitch(firstEventDateUtc, end)) {
                forwardLastEventEndsAtByOneDay()
            }
        }
    }

    private fun isDaySwitch(startUtc: Long, endUtc: Long): Boolean {
        val startDay = Moment(startUtc).monthDay
        val endDay = Moment(endUtc).monthDay
        return startDay != endDay
    }

    private fun forwardLastEventEndsAtByOneDay() {
        lastEventEndsAt += ONE_DAY
    }

    companion object {
        @VisibleForTesting
        const val ONE_DAY = 24 * 60
    }

}
