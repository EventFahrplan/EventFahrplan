package nerd.tuxmobil.fahrplan.congress.schedule

import android.net.Uri

sealed interface MainViewEvent {
    data object OnSettingsUrlChange : MainViewEvent
    data class OnSessionAlarmNotificationTapped(val notificationId: Int) : MainViewEvent
    data object OnAboutClick : MainViewEvent
    data class OnScreenCreated(val notificationsEnabled: Boolean) : MainViewEvent
    data class OnSessionItemClick(val sessionId: String) : MainViewEvent
    data class OnAppLinkReceived(val uri: Uri) : MainViewEvent
    data class OnChangeStatisticsFavoritesFilterToggle(val enabled: Boolean) : MainViewEvent
    data object OnChangeStatisticsConfirm : MainViewEvent
    data object OnChangeStatisticsDismiss : MainViewEvent
    data object OnErrorMessageDismiss : MainViewEvent
}
