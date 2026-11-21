package nerd.tuxmobil.fahrplan.congress.schedule

import android.net.Uri

sealed interface MainViewEvent {
    data class OnScheduleUpdateRequest(val isUserRequest: Boolean) : MainViewEvent
    data class OnSessionAlarmNotificationTapped(val notificationId: Int) : MainViewEvent
    data object OnAboutClick : MainViewEvent
    data object OnCheckPostNotificationsPermission : MainViewEvent
    data class OnSessionItemClick(val sessionId: String) : MainViewEvent
    data class OnAppLinkReceived(val uri: Uri) : MainViewEvent
    data class OnChangeStatisticsFavoritesFilterToggle(val enabled: Boolean) : MainViewEvent
    data class OnCloseChangeStatisticsScreen(val shouldOpenSessionChanges: Boolean) : MainViewEvent
    data object OnCloseErrorMessageScreen : MainViewEvent
}
