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
     */
    @Deprecated("Make ofSessions public and use it and make Conference immutable as soon as Moment is used.")
    fun calculateTimeFrame(sessions: List<Session>) {
        val conference = ofSessions(sessions)
        firstSessionStartsAt = conference.firstSessionStartsAt
        lastSessionEndsAt = conference.lastSessionEndsAt
    }

    companion object {

        /**
         * Creates a [Conference] from the given chronologically sorted [sessions].
         */
        private fun ofSessions(sessions: List<Session>): Conference {
            require(sessions.isNotEmpty()) { "Empty list of sessions." }
            val first = Moment.ofEpochMilli(sessions.first().dateUTC)
            val endingLatest = sessions.endingLatest()
            val endsAt = endingLatest.endsAtDateUtc
            val last = Moment.ofEpochMilli(endsAt)
            val minutesToAdd = if (first.monthDay == last.monthDay) 0 else MINUTES_OF_ONE_DAY
            return Conference(firstSessionStartsAt = first.minuteOfDay, lastSessionEndsAt = last.minuteOfDay + minutesToAdd)
        }

    }

}

/**
 * Returns the [Session] which ends the latest compared to all other [sessions][this].
 */
private fun List<Session>.endingLatest(): Session {
    var endsAt = 0L
    var latestSession = first()
    map { it to it.endsAtDateUtc }.forEach { (session, sessionEndsAt) ->
        if (endsAt == 0L || sessionEndsAt > endsAt) {
            latestSession = session
            endsAt = sessionEndsAt
        }
    }
    return latestSession
}
