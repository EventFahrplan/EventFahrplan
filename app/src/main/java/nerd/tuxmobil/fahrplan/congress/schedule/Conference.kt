package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.ZoneOffset

/**
 * Represents "a conference day" by holding the time values of when the first session of that
 * conference day starts and when the last session of that conference day ends. Please note
 * that "a conference day" does not need to be equivalent with "a natural day" starting at
 * 00:00:00 and ending at 23:59:59. Such "a conferences day" might very likely exceed the
 * natural borders of a day.
 *
 * The [timeZoneOffset] held by this [Conference] object represents the time zone offset for
 * the whole conference day. It is derived from the time zone offset of the first session of
 * that day!
 *
 * TODO: TechDebt: This implementation makes it impossible to represent a conference day which spans
 * two different time zones. Refactoring this is a topic for a future enhancement.
 */
// TODO Use Moment class, merge with ConferenceTimeFrame class?
data class Conference(

        val firstSessionStartsAt: Moment,
        val lastSessionEndsAt: Moment,
        var timeZoneOffset: ZoneOffset? = null,
        val spansMultipleDays: Boolean

) {

    companion object {

        /**
         * Creates a [Conference] from the given chronologically sorted [sessions].
         */
        @JvmStatic
        fun ofSessions(sessions: List<Session>): Conference {
            require(sessions.isNotEmpty()) { "Empty list of sessions." }
            val firstSession = sessions.first()
            val first = Moment.ofEpochMilli(firstSession.dateUTC)
            // TODO Replace with firstSession.toStartsAtMoment() once Session#relStartTime is no longer used.
            val endingLatest = sessions.endingLatest()
            val endsAt = endingLatest.endsAtDateUtc
            val last = Moment.ofEpochMilli(endsAt)
            val minutesToAdd = if (first.monthDay == last.monthDay) 0 else MINUTES_OF_ONE_DAY
            // Here we are assuming all sessions have the same time zone offset.
            val timeZoneOffset = firstSession.timeZoneOffset
            val veryLast = last.plusMinutes(minutesToAdd.toLong())
            return Conference(
                firstSessionStartsAt = first,
                lastSessionEndsAt = veryLast,
                timeZoneOffset = timeZoneOffset,
                spansMultipleDays = minutesToAdd > 0
            )
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
