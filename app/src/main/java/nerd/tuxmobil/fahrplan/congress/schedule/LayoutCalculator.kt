package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.ViewGroup
import android.widget.LinearLayout
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.Duration
import kotlin.collections.set

data class LayoutCalculator(val logging: Logging = Logging.get(),
                            val standardHeight: Int) {

    private companion object {
        const val LOG_TAG = "LayoutCalculator"
        const val DIVISOR = 5
        const val MILLIS_PER_MINUTE = 60000
    }

    fun calculateDisplayDistance(minutes: Int): Int {
        return standardHeight * minutes / DIVISOR
    }

    fun calculateLayoutParams(roomData: RoomData, conference: Conference): Map<Session, LinearLayout.LayoutParams> {
        val sessions = roomData.sessions
        var previousSessionEndsAt: Int = conference.firstSessionStartsAt
        var startTime: Int
        var margin: Int
        var previousSession: Session? = null
        val layoutParamsBySession = mutableMapOf<Session, LinearLayout.LayoutParams>()

        for (sessionIndex in sessions.indices) {
            val session = sessions[sessionIndex]

            startTime = getStartTime(session, previousSessionEndsAt)

            if (startTime > previousSessionEndsAt) {
                // consecutive session
                margin = calculateDisplayDistance(startTime - previousSessionEndsAt)
                if (previousSession != null) {
                    layoutParamsBySession[previousSession]!!.bottomMargin = margin
                    margin = 0
                }
            } else {
                // first session
                margin = 0
            }

            fixOverlappingSessions(sessionIndex, sessions)

            if (!layoutParamsBySession.containsKey(session)) {
                layoutParamsBySession[session] = createLayoutParams(session)
            }

            layoutParamsBySession[session]!!.topMargin = margin
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
            startTime = Moment(session.dateUTC).minuteOfDay
            if (startTime < previousSessionEndsAt) {
                startTime += Duration.ofDays(1).toMinutes().toInt()
            }
        } else {
            startTime = session.relStartTime
        }
        return startTime
    }

    private fun fixOverlappingSessions(sessionIndex: Int, sessions: List<Session>) {
        val session = sessions[sessionIndex]
        val next = sessions.getOrNull(sessionIndex + 1)

        if (next != null && next.dateUTC > 0) {
            val endTimestamp = session.dateUTC + session.duration * MILLIS_PER_MINUTE
            val nextStartsBeforeCurrentEnds = endTimestamp > next.dateUTC
            if (nextStartsBeforeCurrentEnds) {
                logging.d(LOG_TAG, """Collision: "${session.title}" + "${next.title}"""")
                // cut current at the end, to match next sessions start time
                session.duration = ((next.dateUTC - session.dateUTC) / MILLIS_PER_MINUTE).toInt()
            }
        }
    }
}
