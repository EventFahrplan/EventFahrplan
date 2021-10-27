package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices.PendingIntentDelegate
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import org.junit.Test
import org.mockito.kotlin.mock

class AlarmServicesTest {

    private var alarmManager = mock<AlarmManager>()
    private var mockContext = mock<Context>()
    private var pendingIntent = mock<PendingIntent>()
    private val alarm = SchedulableAlarm(3, "1001", "Welcome", 700)

    @Test
    fun `scheduleSessionAlarm invokes cancel then set when discardExisting is true`() {
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, AlarmReceiver.ALARM_SESSION)
                return pendingIntent
            }

        }
        val alarmServices = AlarmServices(alarmManager, pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(mockContext, alarm, true)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `scheduleSessionAlarm only invokes set when discardExisting is false`() {
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, AlarmReceiver.ALARM_SESSION)
                return pendingIntent
            }

        }
        val alarmServices = AlarmServices(alarmManager, pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(mockContext, alarm, false)
        verifyInvokedNever(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `discardSessionAlarm invokes cancel`() {
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, AlarmReceiver.ALARM_DELETE)
                return pendingIntent
            }
        }
        val alarmServices = AlarmServices(alarmManager, pendingIntentDelegate)
        alarmServices.discardSessionAlarm(mockContext, alarm)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
    }


    @Test
    fun `discardAutoUpdateAlarm invokes cancel`() {
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
                assertThat(intent.action).isEqualTo(AlarmReceiver.ALARM_UPDATE)
                return pendingIntent
            }
        }
        val alarmServices = AlarmServices(alarmManager, pendingIntentDelegate)
        alarmServices.discardAutoUpdateAlarm(mockContext)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
    }

    // TODO Move into a unit test for AlarmReceiver once it is written.
    private fun assertIntentExtras(intent: Intent, action: String) {
        assertThat(intent.getIntExtra(BundleKeys.ALARM_DAY, 9)).isEqualTo(alarm.day)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_SESSION_ID)).isEqualTo(alarm.sessionId)
        assertThat(intent.getLongExtra(BundleKeys.ALARM_START_TIME, 0)).isEqualTo(alarm.startTime)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_TITLE)).isEqualTo(alarm.sessionTitle)
        assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
        assertThat(intent.action).isEqualTo(action)
        assertThat(intent.data).isEqualTo("alarm://${alarm.sessionId}".toUri())
    }

}
