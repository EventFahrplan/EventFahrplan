package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
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

        val timeFrame: ClosedRange<Moment>,
        var timeZoneOffset: ZoneOffset? = null,
        val spansMultipleDays: Boolean

) {

    val firstSessionStartsAt = timeFrame.start
    val lastSessionEndsAt = timeFrame.endInclusive

    companion object {

        /**
         * Creates a [Conference] from the given chronologically sorted [sessions].
         */
        fun ofSessions(sessions: List<Session>): Conference {
            require(sessions.isNotEmpty()) { "Empty list of sessions." }
            val firstSession = sessions.first()
            val first = Moment.ofEpochMilli(firstSession.dateUTC)
            // TODO Replace with firstSession.toStartsAtMoment() once Session#relStartTime is no longer used.
            val endingLatest = sessions.endingLatest()
            val last = endingLatest.endsAt
            // Here we are assuming all sessions have the same time zone offset.
            val timeZoneOffset = firstSession.timeZoneOffset
            return Conference(
                timeFrame = first..last,
                timeZoneOffset = timeZoneOffset,
                spansMultipleDays = first.monthDay != last.monthDay
            )
        }

    }

}

/**
 * Returns the [Session] which ends the latest compared to all other [sessions][this].
 */
private fun List<Session>.endingLatest(): Session {
    var endsAt = Moment.ofEpochMilli(0L)
    var latestSession = first()
    map { it to it.endsAt }.forEach { (session, sessionEndsAt) ->
        if (endsAt.isSimultaneousWith(Moment.ofEpochMilli(0L)) || sessionEndsAt.isAfter(endsAt)) {
            latestSession = session
            endsAt = sessionEndsAt
        }
    }
    return latestSession
}
