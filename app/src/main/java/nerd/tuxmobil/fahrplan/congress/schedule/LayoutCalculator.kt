package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.Duration
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.getOrNull
import kotlin.collections.indices
import kotlin.collections.mutableMapOf
import kotlin.collections.set

private typealias SessionId = String

data class LayoutCalculator @JvmOverloads constructor(

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

        for (sessionIndex in sessions.indices) {
            val session = sessions[sessionIndex]

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

            fixOverlappingSessions(sessionIndex, sessions)

            if (!layoutParamsBySession.containsKey(session.sessionId)) {
                layoutParamsBySession[session.sessionId] = createLayoutParams(session)
            }

            layoutParamsBySession[session.sessionId]!!.topMargin = margin
            previousSessionEndsAt = startTime + session.duration
            previousSession = session
        }

        return layoutParamsBySession
    }

    private fun createLayoutParams(session: Session): LinearLayout.LayoutParams {
        val height = calculateDisplayDistance(session.duration)
        val marginLayoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return LinearLayout.LayoutParams(marginLayoutParams)
    }

    private fun getStartTime(session: Session, previousSessionEndsAt: Int): Int {
        var startTime: Int
        if (session.dateUTC > 0) {
            startTime = session.startsAt.minuteOfDay
            if (startTime < previousSessionEndsAt) {
                startTime += Duration.ofDays(1).toMinutes().toInt()
            }
        } else {
            startTime = session.relStartTime
        }
        return startTime
    }

    @VisibleForTesting
    fun fixOverlappingSessions(sessionIndex: Int, sessions: List<Session>) {
        val session = sessions[sessionIndex]
        val next = sessions.getOrNull(sessionIndex + 1)

        if (next != null && next.dateUTC > 0) {
            val nextStartsBeforeCurrentEnds = next.startsAt.isBefore(session.endsAt)
            if (nextStartsBeforeCurrentEnds) {
                logging.d(LOG_TAG, """Collision: "${session.title}" + "${next.title}"""")
                // cut current at the end, to match next sessions start time
                session.duration = session.startsAt.minutesUntil(next.startsAt).toInt()
            }
        }
    }
}
