package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session

sealed interface FahrplanViewEvent {
    data class OnScheduleUpdateRequest(val isUserRequest: Boolean) : FahrplanViewEvent
    data object OnConnectionAvailable : FahrplanViewEvent
    data class OnFillTimes(val nowMoment: Moment, val normalizedBoxHeight: Int) : FahrplanViewEvent
    data class OnFavoriteStatusChange(val session: Session) : FahrplanViewEvent
    data object OnAddAlarmWithChecks : FahrplanViewEvent
    data class OnAddAlarmClick(val session: Session) : FahrplanViewEvent
    data class OnDeleteAlarmClick(val session: Session) : FahrplanViewEvent
    data class OnAlarmTimePicked(val alarmTime: Int) : FahrplanViewEvent
    data object OnNoAlarmTimePicked : FahrplanViewEvent
    data class OnShareClick(val session: Session) : FahrplanViewEvent
    data class OnShareToChaosflixClick(val session: Session) : FahrplanViewEvent
    data class OnDaySelect(val dayItemPosition: Int) : FahrplanViewEvent
    data class OnSaveSelectedDayIndex(val dayIndex: Int) : FahrplanViewEvent
    data object OnScrollToCurrentSession : FahrplanViewEvent
    data class OnScrollToSession(val sessionId: String, val boxHeight: Int) : FahrplanViewEvent
    data object OnCloseErrorMessageScreen : FahrplanViewEvent
}
