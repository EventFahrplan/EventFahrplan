package nerd.tuxmobil.fahrplan.congress.alarms

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class SessionAlarmViewModelDelegateTest {

    private companion object {
        val SESSION = Session("session-23")
    }

    @Test
    fun `addAlarm() invokes addSessionAlarm() function`() = runTest {
        val alarmServices = mock<AlarmServices>()
        val delegate = createDelegate(alarmServices = alarmServices)
        delegate.addAlarm(SESSION, alarmTimesIndex = 0)
        verifyInvokedOnce(alarmServices).addSessionAlarm(SESSION, alarmTimesIndex = 0)
    }

    @Test
    fun `deleteAlarm() invokes deleteSessionAlarm() function`() = runTest {
        val alarmServices = mock<AlarmServices>()
        val delegate = createDelegate(alarmServices = alarmServices)
        delegate.deleteAlarm(SESSION)
        verifyInvokedOnce(alarmServices).deleteSessionAlarm(SESSION)
    }

    @Test
    fun `canAddAlarms() invokes canScheduleExactAlarms property`() = runTest {
        val alarmServices = mock<AlarmServices>()
        val delegate = createDelegate(alarmServices = alarmServices)
        delegate.canAddAlarms()
        verifyInvokedOnce(alarmServices).canScheduleExactAlarms
    }

    @Test
    fun `addAlarmWithChecks() posts to showAlarmTimePicker`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val alarmServices = mock<AlarmServices> {
            on { canScheduleExactAlarms } doReturn true
        }
        val delegate = createDelegate(notificationHelper, alarmServices)
        delegate.addAlarmWithChecks()
        delegate.showAlarmTimePicker.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `addAlarmWithChecks() posts to requestScheduleExactAlarmsPermission`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn true
        }
        val alarmServices = mock<AlarmServices> {
            on { canScheduleExactAlarms } doReturn false
        }
        val delegate = createDelegate(notificationHelper, alarmServices)
        delegate.addAlarmWithChecks()
        delegate.requestScheduleExactAlarmsPermission.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `addAlarmWithChecks() posts to requestPostNotificationsPermission as of Android 13`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn false
        }
        val delegate = createDelegate(
            notificationHelper = notificationHelper,
            runsAtLeastOnAndroidTiramisu = true,
        )
        delegate.addAlarmWithChecks()
        delegate.requestPostNotificationsPermission.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `addAlarmWithChecks() posts to notificationsDisabled before Android 13`() = runTest {
        val notificationHelper = mock<NotificationHelper> {
            on { notificationsEnabled } doReturn false
        }
        val delegate = createDelegate(
            notificationHelper = notificationHelper,
            runsAtLeastOnAndroidTiramisu = false,
        )
        delegate.addAlarmWithChecks()
        delegate.notificationsDisabled.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    private fun TestScope.createDelegate(
        notificationHelper: NotificationHelper = mock(),
        alarmServices: AlarmServices = mock(),
        runsAtLeastOnAndroidTiramisu: Boolean = true,
    ) = SessionAlarmViewModelDelegate(
        viewModelScope = this,
        notificationHelper = notificationHelper,
        alarmServices = alarmServices,
        runsAtLeastOnAndroidTiramisu = runsAtLeastOnAndroidTiramisu,
    )

}
