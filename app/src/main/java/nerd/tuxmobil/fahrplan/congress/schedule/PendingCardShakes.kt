package nerd.tuxmobil.fahrplan.congress.schedule

import android.os.SystemClock
import androidx.compose.runtime.mutableStateMapOf
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import kotlin.time.Duration.Companion.seconds

/**
 * Shake requests for session cards whose alarm fired.
 *
 * The requests are kept in Compose snapshot state ([mutableStateMapOf]). Compose records which
 * snapshot state a composable reads while composing and recomposes it when that state changes.
 * Room columns read the requests via [isRequested], so a new request makes an already composed
 * column recompose in place, without the fragment re-rendering it via setContent. Its open context
 * menu, running animations and TalkBack focus are therefore kept.
 *
 * A request stays until its card finished shaking, so a column which is re-rendered while a card
 * shakes restarts the shake while a re-render afterward does not replay it.
 *
 * The firing event and the schedule update which removes the alarm arrive in no particular order.
 * Therefore, a card only shakes once it is shown without its alarm.
 *
 * A request expires [MAX_SHAKE_DELAY] after the alarm fired. This prevents a late shake, e.g. when
 * the user scrolls to a room column which was off-screen when the alarm fired or when the schedule
 * returns to the foreground long after. Following the alarm notification right away still shakes.
 */
class PendingCardShakes(
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
) {

    companion object {
        val MAX_SHAKE_DELAY = 30.seconds
    }

    private val firedAtBySessionId = mutableStateMapOf<String, Long>()
    private var displayedSessionIds = emptySet<String>()

    fun isRequested(sessionId: String, hasAlarm: Boolean): Boolean {
        if (hasAlarm) {
            return false
        }
        val firedAt = firedAtBySessionId[sessionId] ?: return false
        return !isExpired(firedAt)
    }

    /**
     * Requests a shake for the given [sessionId] whose alarm fired at the given [firedAt]
     * elapsed realtime. It is ignored if it already expired or if the session is not part of the
     * displayed schedule, e.g. because it takes place on a different day.
     */
    fun request(sessionId: String, firedAt: Long) {
        if (sessionId in displayedSessionIds && !isExpired(firedAt)) {
            firedAtBySessionId[sessionId] = firedAt
        }
    }

    /**
     * Drops expired requests and requests for sessions which are not part of the given
     * [scheduleData], e.g. after switching days or when a session has been removed.
     */
    fun update(scheduleData: ScheduleData) {
        displayedSessionIds = scheduleData.allSessions.mapTo(mutableSetOf()) { it.sessionId }
        if (firedAtBySessionId.isNotEmpty()) {
            firedAtBySessionId.entries.removeAll { (sessionId, firedAt) ->
                sessionId !in displayedSessionIds || isExpired(firedAt)
            }
        }
    }

    fun onShakeFinished(sessionId: String) {
        firedAtBySessionId.remove(sessionId)
    }

    private fun isExpired(firedAt: Long) =
        elapsedRealtime() - firedAt > MAX_SHAKE_DELAY.inWholeMilliseconds

}
