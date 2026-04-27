package nerd.tuxmobil.fahrplan.congress.details

sealed interface SessionDetailsViewEvent {
    data class OnSessionLinkClick(val link: String) : SessionDetailsViewEvent
    data object OnOpenFeedbackClick : SessionDetailsViewEvent
    data object OnShareClick : SessionDetailsViewEvent
    data object OnShareToChaosflixClick : SessionDetailsViewEvent
    data object OnAddToCalendarClick : SessionDetailsViewEvent
    data object OnAddFavoriteClick : SessionDetailsViewEvent
    data object OnDeleteFavoriteClick : SessionDetailsViewEvent
    data object OnAddAlarmWithChecks : SessionDetailsViewEvent
    data class OnAddAlarm(val alarmTime: Int) : SessionDetailsViewEvent
    data object OnDeleteAlarmClick : SessionDetailsViewEvent
    data object OnNavigateToRoomClick : SessionDetailsViewEvent
}
