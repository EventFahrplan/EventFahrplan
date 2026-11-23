package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session

sealed interface FahrplanViewEvent {
    data object OnRefreshClick : FahrplanViewEvent
    data object OnConnectionAvailable : FahrplanViewEvent
    data class OnScheduleDataReceived(val nowMoment: Moment, val normalizedBoxHeight: Int) : FahrplanViewEvent
    data class OnToggleFavoriteClick(val session: Session) : FahrplanViewEvent
    data object OnAddAlarmsPermissionGranted : FahrplanViewEvent
    data class OnAddAlarmClick(val session: Session) : FahrplanViewEvent
    data class OnDeleteAlarmClick(val session: Session) : FahrplanViewEvent
    data class OnAlarmTimePicked(val alarmTime: Int) : FahrplanViewEvent
    data object OnNoAlarmTimePicked : FahrplanViewEvent
    data class OnShareClick(val session: Session) : FahrplanViewEvent
    data class OnShareToChaosflixClick(val session: Session) : FahrplanViewEvent
    data object OnDayRendered : FahrplanViewEvent
    data class OnDaySelect(val dayItemPosition: Int) : FahrplanViewEvent
    data class OnSessionAlarmNotificationTapped(val dayIndex: Int, val sessionId: String, val normalizedBoxHeight: Int) : FahrplanViewEvent
    data object OnErrorMessageDismiss : FahrplanViewEvent
}
