package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import kotlin.collections.set

private typealias SessionId = String

data class LayoutCalculator(

        val standardHeight: Int,
        val logging: Logging = Logging.get()

) {

    private companion object {
        const val LOG_TAG = "LayoutCalculator"
        const val DIVISOR = 5
    }

    fun calculateDisplayDistance(minutes: Int): Int {
        return standardHeight * minutes / DIVISOR
    }

    fun calculateLayoutParams(roomData: RoomData, conference: Conference): Map<SessionId, LinearLayout.LayoutParams> {
        val sessions = roomData.sessions
        var previousSessionEndsAt: Int = conference.firstSessionStartsAt.minuteOfDay
        var startTime: Int
        var margin: Int
        var previousSession: Session? = null
        val layoutParamsBySession = mutableMapOf<SessionId, LinearLayout.LayoutParams>()

        for ((index, session) in sessions.withIndex()) {
            startTime = getStartTime(session, previousSessionEndsAt)

            if (startTime > previousSessionEndsAt) {
                // consecutive session
                margin = calculateDisplayDistance(startTime - previousSessionEndsAt)
                if (previousSession != null) {
                    layoutParamsBySession[previousSession.sessionId]!!.bottomMargin = margin
                    margin = 0
                }
            } else {
                // first session
                margin = 0
            }

            val adjustedSession = if (index < sessions.lastIndex) {
                val nextSession = sessions[index + 1]
                fixOverlappingSessions(session, nextSession)
            } else {
                session
            }

            val sessionId = adjustedSession.sessionId
            if (!layoutParamsBySession.containsKey(sessionId)) {
                layoutParamsBySession[sessionId] = createLayoutParams(adjustedSession)
            }

            layoutParamsBySession[sessionId]!!.topMargin = margin
            previousSessionEndsAt = startTime + adjustedSession.duration.toWholeMinutes().toInt()
            previousSession = adjustedSession
        }

        return layoutParamsBySession
    }

    private fun createLayoutParams(session: Session): LinearLayout.LayoutParams {
        val height = calculateDisplayDistance(session.duration.toWholeMinutes().toInt())
        val marginLayoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return LinearLayout.LayoutParams(marginLayoutParams)
    }

    private fun getStartTime(session: Session, previousSessionEndsAt: Int): Int {
        var startTime: Int
        if (session.dateUTC > 0) {
            startTime = session.startsAt.minuteOfDay
            if (startTime < previousSessionEndsAt) {
                startTime += Duration.ofDays(1).toWholeMinutes().toInt()
            }
        } else {
            startTime = session.relativeStartTime.toWholeMinutes().toInt()
        }
        return startTime
    }

    @VisibleForTesting
    fun fixOverlappingSessions(session: Session, next: Session): Session {
        return if (next.dateUTC > 0 && next.startsAt.isBefore(session.endsAt)) {
            logging.d(LOG_TAG, """Collision: "${session.title}" + "${next.title}"""")
            // cut current at the end, to match next sessions start time
            val newDuration = session.startsAt.durationUntil(next.startsAt)
            session.copy(duration = newDuration)
        } else {
            session
        }
    }
}
