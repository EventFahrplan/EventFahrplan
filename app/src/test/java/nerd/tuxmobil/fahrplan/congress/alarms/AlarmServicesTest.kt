package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import org.junit.Test

class AlarmServicesTest {

    private var alarmManager = mock<AlarmManager>()
    private var mockContext = mock<Context>()
    private var pendingIntent = mock<PendingIntent>()
    private val alarm = SchedulableAlarm(3, "1001", "Welcome", 700)

    @Test
    fun `scheduleEventAlarm invokes "cancel" then "set" when "discardExisting" is "true"`() {
        val onPendingIntentBroadcast: PendingIntentCallback = { context, requestCode, intent, flags ->
            assertThat(context).isEqualTo(mockContext)
            assertThat(requestCode).isEqualTo(alarm.eventId.toInt())
            assertIntentExtras(intent, AlarmReceiver.ALARM_LECTURE)
            assertThat(flags).isEqualTo(0)
            pendingIntent
        }
        val alarmServices = AlarmServices(alarmManager, onPendingIntentBroadcast)
        alarmServices.scheduleEventAlarm(mockContext, alarm, true)
        verify(alarmManager, once()).cancel(pendingIntent)
        verify(alarmManager, once()).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `scheduleEventAlarm only invokes "set" when "discardExisting" is "false"`() {
        val onPendingIntentBroadcast: PendingIntentCallback = { context, requestCode, intent, flags ->
            assertThat(context).isEqualTo(mockContext)
            assertThat(requestCode).isEqualTo(alarm.eventId.toInt())
            assertIntentExtras(intent, AlarmReceiver.ALARM_LECTURE)
            assertThat(flags).isEqualTo(0)
            pendingIntent
        }
        val alarmServices = AlarmServices(alarmManager, onPendingIntentBroadcast)
        alarmServices.scheduleEventAlarm(mockContext, alarm, false)
        verify(alarmManager, never()).cancel(pendingIntent)
        verify(alarmManager, once()).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `discardEventAlarm invokes "cancel"`() {
        val onPendingIntentBroadcast: PendingIntentCallback = { context, requestCode, intent, flags ->
            assertThat(context).isEqualTo(mockContext)
            assertThat(requestCode).isEqualTo(alarm.eventId.toInt())
            assertIntentExtras(intent, AlarmReceiver.ALARM_DELETE)
            assertThat(flags).isEqualTo(0)
            pendingIntent
        }
        val alarmServices = AlarmServices(alarmManager, onPendingIntentBroadcast)
        alarmServices.discardEventAlarm(mockContext, alarm)
        verify(alarmManager, once()).cancel(pendingIntent)
    }


    @Test
    fun `discardAutoUpdateAlarm invokes "cancel"`() {
        val onPendingIntentBroadcast: PendingIntentCallback = { context, requestCode, intent, flags ->
            assertThat(context).isEqualTo(mockContext)
            assertThat(requestCode).isEqualTo(0)
            assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
            assertThat(intent.action).isEqualTo(AlarmReceiver.ALARM_UPDATE)
            assertThat(flags).isEqualTo(0)
            pendingIntent
        }
        val alarmServices = AlarmServices(alarmManager, onPendingIntentBroadcast)
        alarmServices.discardAutoUpdateAlarm(mockContext)
        verify(alarmManager, once()).cancel(pendingIntent)
    }

    // TODO Move into a unit test for AlarmReceiver once it is written.
    private fun assertIntentExtras(intent: Intent, action: String) {
        assertThat(intent.getIntExtra(BundleKeys.ALARM_DAY, 9)).isEqualTo(alarm.day)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_LECTURE_ID)).isEqualTo(alarm.eventId)
        assertThat(intent.getLongExtra(BundleKeys.ALARM_START_TIME, 0)).isEqualTo(alarm.startTime)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_TITLE)).isEqualTo(alarm.eventTitle)
        assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
        assertThat(intent.action).isEqualTo(action)
        assertThat(intent.data).isEqualTo(Uri.parse("alarm://${alarm.eventId}"))
    }

    private fun once() = times(1)

}
