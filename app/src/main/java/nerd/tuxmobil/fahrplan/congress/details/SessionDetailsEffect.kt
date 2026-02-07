package nerd.tuxmobil.fahrplan.congress.details

import android.net.Uri
import nerd.tuxmobil.fahrplan.congress.models.Session

internal sealed interface SessionDetailsEffect {
    data class OpenFeedback(val uri: Uri) : SessionDetailsEffect
    data class ShareSimple(val formattedSession: String) : SessionDetailsEffect
    data class ShareJson(val formattedSession: String) : SessionDetailsEffect
    data class AddToCalendar(val session: Session) : SessionDetailsEffect
    data class NavigateToRoom(val uri: Uri) : SessionDetailsEffect
    data object CloseDetails : SessionDetailsEffect
    data object ShowAlarmTimePicker : SessionDetailsEffect
    data object RequestPostNotificationsPermission : SessionDetailsEffect
    data object RequestScheduleExactAlarmsPermission : SessionDetailsEffect
    data object ShowNotificationsDisabledError : SessionDetailsEffect
}
