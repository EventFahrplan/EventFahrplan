package nerd.tuxmobil.fahrplan.congress.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver.AlarmIntentFactory.Companion.ALARM_DELETE
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver.AlarmIntentFactory.Companion.ALARM_SESSION
import nerd.tuxmobil.fahrplan.congress.commons.PendingIntentDelegate
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.getMomentExtra
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test
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
    private val alarm = SchedulableAlarm(
        dayIndex = 3,
        sessionId = "1001",
        sessionTitle = "Welcome",
        startTime = Moment.ofEpochMilli(700),
    )

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
        val alarmServices = createAlarmServices()
        whenever(repository.readUseDeviceTimeZoneEnabled()) doReturn true
        val session = Session(
            sessionId = "S1",
            dayIndex = 1,
            title = "Title",
            dateUTC = 1536332400000L, // 2018-09-07T17:00:00+02:00
            timeZoneOffset = ZoneOffset.of("+02:00"),
        )

        alarmServices.addSessionAlarm(session, 60)
        // AlarmServices invokes scheduleSessionAlarm() which is tested separately.
        val expectedAlarm = Alarm(
            dayIndex = 1,
            sessionId = "S1",
            sessionTitle = "Title",
            startTime = Moment.ofEpochMilli(1536328800000),
        )
        verifyInvokedOnce(repository).updateAlarm(expectedAlarm)
    }

    @Test
    fun `deleteSessionAlarm discards a session alarm when the alarm was scheduled`() {
        val alarmServices = createAlarmServices()
        val alarm = Alarm(
            dayIndex = 2,
            sessionId = "S3",
            sessionTitle = "Title",
            startTime = Moment.ofEpochMilli(1536332400000L),
        )
        whenever(repository.readAlarms(any())) doReturn listOf(alarm)
        val session = Session(sessionId = "S3", hasAlarm = true)

        alarmServices.deleteSessionAlarm(session)
        // AlarmServices invokes discardSessionAlarm() which is tested separately.
        verifyInvokedOnce(repository).deleteAlarmForSessionId("S3")
    }

    @Test
    fun `deleteSessionAlarm returns without action when no alarm was scheduled`() {
        val pendingIntentDelegate = mock<PendingIntentDelegate>()
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        whenever(repository.readAlarms(any())) doReturn emptyList()
        val session = Session(sessionId = "S4")

        alarmServices.deleteSessionAlarm(session)
        verifyNoInteractions(pendingIntentDelegate)
    }

    @Test
    fun `canScheduleExactAlarms returns true because alarmManager returns true in unit test context`() {
        val alarmManager = mock<AlarmManager> {
            // always true because SDK_INT = 0 in tests, see AlarmManagerCompat#canScheduleExactAlarms
            on { canScheduleExactAlarms() } doReturn true
        }
        val alarmServices = createAlarmServices(
            alarmManager = alarmManager,
        )
        assertThat(alarmServices.canScheduleExactAlarms).isTrue()
    }

    @Test
    fun `scheduleSessionAlarm invokes cancel then set when discardExisting is true`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = PendingIntentBroadcastProvider { context, intent ->
            assertThat(context).isEqualTo(mockContext)
            assertIntentExtras(intent, ALARM_SESSION)
            pendingIntent
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(alarm, true)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).setExact(AlarmManager.RTC_WAKEUP, alarm.startTime.toMilliseconds(), pendingIntent)
    }

    @Test
    fun `scheduleSessionAlarm only invokes set when discardExisting is false`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = PendingIntentBroadcastProvider { context, intent ->
            assertThat(context).isEqualTo(mockContext)
            assertIntentExtras(intent, ALARM_SESSION)
            pendingIntent
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.scheduleSessionAlarm(alarm, false)
        verifyInvokedNever(alarmManager).cancel(pendingIntent)
        verifyInvokedOnce(alarmManager).setExact(AlarmManager.RTC_WAKEUP, alarm.startTime.toMilliseconds(), pendingIntent)
    }

    @Test
    fun `discardSessionAlarm invokes cancel`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = PendingIntentBroadcastProvider { context, intent ->
            assertThat(context).isEqualTo(mockContext)
            assertIntentExtras(intent, ALARM_DELETE)
            pendingIntent
        }
        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.discardSessionAlarm(alarm)
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
    }


    @Test
    fun `discardAutoUpdateAlarm invokes cancel`() {
        val pendingIntent = mock<PendingIntent>()
        val pendingIntentDelegate = PendingIntentBroadcastProvider { context, intent ->
            assertThat(context).isEqualTo(mockContext)
            assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
            assertThat(intent.action).isEqualTo(AlarmReceiver.ALARM_UPDATE)
            pendingIntent
        }

        val alarmServices = createAlarmServices(pendingIntentDelegate)
        alarmServices.discardAutoUpdateAlarm()
        verifyInvokedOnce(alarmManager).cancel(pendingIntent)
    }

    // TODO Move into a unit test for AlarmReceiver once it is written.
    private fun assertIntentExtras(intent: Intent, action: String) {
        assertThat(intent.getIntExtra(BundleKeys.ALARM_DAY_INDEX, 9)).isEqualTo(alarm.dayIndex)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_SESSION_ID)).isEqualTo(alarm.sessionId)
        assertThat(intent.getMomentExtra(BundleKeys.ALARM_START_TIME, Moment.ofEpochMilli(0))).isEqualTo(alarm.startTime)
        assertThat(intent.getStringExtra(BundleKeys.ALARM_TITLE)).isEqualTo(alarm.sessionTitle)
        assertThat(intent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
        assertThat(intent.action).isEqualTo(action)
        assertThat(intent.data).isEqualTo("alarm://${alarm.sessionId}".toUri())
    }

    private fun createAlarmServices(
        pendingIntentDelegate: PendingIntentDelegate = mock(),
        alarmManager: AlarmManager = this.alarmManager,
    ) = AlarmServices(
        context = mockContext,
        repository = repository,
        alarmManager = alarmManager,
        logging = NoLogging,
        pendingIntentDelegate = pendingIntentDelegate,
    )

}

private class PendingIntentBroadcastProvider(
    val action: (context: Context, intent: Intent) -> PendingIntent,
) : PendingIntentDelegate {

    override fun getPendingIntentActivity(context: Context, intent: Intent): PendingIntent {
        throw NotImplementedError("Not needed for this test.")
    }

    override fun getPendingIntentBroadcast(context: Context, intent: Intent): PendingIntent {
        return action(context, intent)
    }

}
