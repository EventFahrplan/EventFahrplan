package nerd.tuxmobil.fahrplan.congress.alarms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper

/**
 * Wraps all concerns related to adding and deleting sessions alarms
 * and the related permission checks. This handling is centralized here
 * to avoid code duplication in the individual view models.
 */
internal class SessionAlarmViewModelDelegate(
    private val viewModelScope: CoroutineScope,
    private val notificationHelper: NotificationHelper,
    private val alarmServices: AlarmServices,
    private val runsAtLeastOnAndroidTiramisu: Boolean,
) {

    private val mutableShowAlarmTimePicker = Channel<Unit>()
    val showAlarmTimePicker = mutableShowAlarmTimePicker
        .receiveAsFlow()

    private val mutableRequestPostNotificationsPermission = Channel<Unit>()
    val requestPostNotificationsPermission = mutableRequestPostNotificationsPermission
        .receiveAsFlow()

    private val mutableNotificationsDisabled = Channel<Unit>()
    val notificationsDisabled = mutableNotificationsDisabled
        .receiveAsFlow()

    fun addAlarmWithChecks() {
        if (notificationHelper.notificationsEnabled) {
            mutableShowAlarmTimePicker.sendOneTimeEvent(Unit)
        } else {
            // Check runtime version here because requesting the POST_NOTIFICATION permission
            // before Android 13 (Tiramisu) has no effect nor error message.
            when (runsAtLeastOnAndroidTiramisu) {
                true -> mutableRequestPostNotificationsPermission.sendOneTimeEvent(Unit)
                false -> mutableNotificationsDisabled.sendOneTimeEvent(Unit)
            }
        }
    }

    fun addAlarm(session: Session, alarmTimesIndex: Int) =
        alarmServices.addSessionAlarm(session, alarmTimesIndex)

    fun deleteAlarm(session: Session) = alarmServices.deleteSessionAlarm(session)

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
