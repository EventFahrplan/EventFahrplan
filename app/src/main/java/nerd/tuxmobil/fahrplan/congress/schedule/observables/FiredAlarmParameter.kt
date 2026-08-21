package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel

/**
 * Payload of the observable [firedAlarmParameter][FahrplanViewModel.firedAlarmParameter]
 * property in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 *
 * [firedAt] is the [elapsed realtime][android.os.SystemClock.elapsedRealtime] in milliseconds at
 * which the alarm fired. The event can be delivered much later, e.g. when the fragment returns to
 * the foreground, so the age of the event must be measured from this point in time.
 */
data class FiredAlarmParameter(
    val sessionId: String,
    val firedAt: Long,
)
