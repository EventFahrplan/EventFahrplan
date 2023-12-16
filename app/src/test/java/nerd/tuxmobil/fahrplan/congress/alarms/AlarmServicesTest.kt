package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver.AlarmIntentFactory.Companion.ALARM_DELETE
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver.AlarmIntentFactory.Companion.ALARM_SESSION
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices.FormattingDelegate
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices.PendingIntentDelegate
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.threeten.bp.ZoneOffset

class AlarmServicesTest {

    private var alarmManager = mock<AlarmManager>()
    private var mockContext = mock<Context>()
    private var repository = mock<AppRepository>()
    private val alarmTimesValues = listOf("0", "10", "60")
    private val alarm = SchedulableAlarm(3, "1001", "Welcome", 700)

    @Test
    fun `newInstance returns a new preconfigured instance of the AlarmServices class`() {
        val resources = mock<Resources> {
            on { getStringArray(any()) } doReturn arrayOf("not relevant")
        }
        val context = mock<Context> {
            on { getResources() } doReturn resources
            on { getSystemService<AlarmManager>() } doReturn mock()
        }
        assertThat(AlarmServices.newInstance(context, repository, NoLogging)).isInstanceOf(AlarmServices::class.java)
    }

    @Test
    fun `addSessionAlarm schedules a session alarm`() {
        val formattingDelegate = mock<FormattingDelegate>()
        val alarmServices = createAlarmServices(formattingDelegate = formattingDelegate)
        whenever(repository.readUseDeviceTimeZoneEnabled()) doReturn true
        whenever(formattingDelegate.getFormattedDateTimeShort(any(), any(), any())) doReturn "not relevant"
        val session = Session("S1").apply {
            day = 1
            hasAlarm = false
            title = "Title"
            dateUTC = 1536332400000L // 2018-09-07T17:00:00+02:00
            timeZoneOffset = ZoneOffset.of("+02:00")
        }

        alarmServices.addSessionAlarm(session, alarmTimesValues.indexOf("60"))
        // AlarmServices invokes scheduleSessionAlarm() which is tested separately.
        assertThat(session.hasAlarm).isTrue()
        val expectedAlarm = Alarm(
            alarmTimeInMin = 60,
            day = 1,
            displayTime = 1536332400000,
            sessionId = "S1",
            sessionTitle = "Title",
            startTime = 1536328800000,
            timeText = "not relevant"
        )
        verifyInvokedOnce(repository).updateAlarm(expectedAlarm)
    }

    @Test
    fun `addSessionAlarm throws exception if alarm time index exceeds `() {
        val pendingIntentDelegate = mock<PendingIntentDelegate>()
        val formattingDelegate = mock<FormattingDelegate>()
        val alarmServices = createAlarmServices(pendingIntentDelegate = pendingIntentDelegate, formattingDelegate = formattingDelegate)
        val session = Session("S2").apply { dateUTC = 1536332400000L } // 2018-09-07T17:00:00+02:00

        try {
            alarmServices.addSessionAlarm(session, alarmTimesValues.size) // Out of bounds!
            fail("Expect an IndexOutOfBoundsException to be thrown.")
        } catch (e: IndexOutOfBoundsException) {
            assertThat(e.message).isEqualTo("Index 3 out of bounds for length 3")
        }
        verifyNoInteractions(pendingIntentDelegate)
        verifyNoInteractions(formattingDelegate)
    }

    @Test
    fun `deleteSessionAlarm discards a session alarm when the alarms was scheduled`() {
        val formattingDelegate = mock<FormattingDelegate>()
        val alarmServices = createAlarmServices(formattingDelegate = formattingDelegate)
        val alarm = Alarm(10, 2, 0, "S3", "Title", 1536332400000L, "Lorem ipsum")
        whenever(repository.readAlarms(any())) doReturn listOf(alarm)
        val session = Session("S3").apply { hasAlarm = true }

        alarmServices.deleteSessionAlarm(session)
        // AlarmServices invokes discardSessionAlarm() which is tested separately.
        verifyNoInteractions(formattingDelegate)
        verifyInvokedOnce(repository).deleteAlarmForSessionId("S3")
        assertThat(session.hasAlarm).isFalse()
    }

    @Test
    fun `deleteSessionAlarm resets the session alarm flag when no alarms was scheduled`() {
        val pendingIntentDelegate = mock<PendingIntentDelegate>()
        val formattingDelegate = mock<FormattingDelegate>()
        val alarmServices = createAlarmServices(pendingIntentDelegate = pendingIntentDelegate, formattingDelegate = formattingDelegate)
        whenever(repository.readAlarms(any())) doReturn emptyList()
        val session = Session("S4").apply { hasAlarm = true }

        alarmServices.deleteSessionAlarm(session)
        verifyNoInteractions(pendingIntentDelegate)
        verifyNoInteractions(formattingDelegate)
        assertThat(session.hasAlarm).isFalse()
    }

    @Test
    fun `scheduleSessionAlarm invokes cancel then set when discardExisting is true`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, ALARM_SESSION)
                return pendingIntent
            }
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(alarm, true)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `scheduleSessionAlarm only invokes set when discardExisting is false`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, ALARM_SESSION)
                return pendingIntent
            }
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(alarm, false)
        verifyInvokedNever(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).set(AlarmManager.RTC_WAKEUP, alarm.startTime, pendingIntent)
    }

    @Test
    fun `discardSessionAlarm invokes cancel`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertIntentExtras(intent, ALARM_DELETE)
                return pendingIntent
            }
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.discardSessionAlarm(alarm)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
    }


    @Test
    fun `discardAutoUpdateAlarm invokes cancel`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = object : PendingIntentDelegate {
            override fun onPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
                assertThat(context).isEqualTo(mockContext)
                assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
                assertThat(intent.action).isEqualTo(AlarmReceiver.ALARM_UPDATE)
                return pendingIntent
            }
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.discardAutoUpdateAlarm()
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

    private fun createAlarmServices(
        pendingIntentDelegate: PendingIntentDelegate = mock(),
        formattingDelegate: FormattingDelegate = mock()
    ) = AlarmServices(
        context = mockContext,
        repository = repository,
        alarmManager = alarmManager,
        alarmTimeValues = alarmTimesValues,
        logging = NoLogging,
        pendingIntentDelegate = pendingIntentDelegate,
        formattingDelegate = formattingDelegate
    )

}
