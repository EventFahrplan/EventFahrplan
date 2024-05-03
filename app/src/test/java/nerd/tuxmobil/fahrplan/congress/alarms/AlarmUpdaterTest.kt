package nerd.tuxmobil.fahrplan.congress.alarms

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater.OnAlarmUpdateListener
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AlarmUpdaterTest {

    private companion object {

        // 2015-12-27T00:00:00+0100, in milliseconds: 1451170800000
        val FIRST_DAY_START_TIME = Moment.ofEpochMilli(1451170800000L)

        // 2015-12-31T00:00:00+0100, in milliseconds: 1451516400000
        val LAST_DAY_END_TIME = Moment.ofEpochMilli(1451516400000L)

        const val NEVER_USED: Long = -1
        val NEVER_USED_MOMENT: Moment = Moment.ofEpochMilli(NEVER_USED)

        const val THREE_SECONDS = 3 * Moment.MILLISECONDS_OF_ONE_SECOND
        const val TWO_HOURS = 2 * Moment.MILLISECONDS_OF_ONE_HOUR
        const val ONE_DAY = Moment.MILLISECONDS_OF_ONE_DAY

        val conferenceTimeFrame = ConferenceTimeFrame.Known(FIRST_DAY_START_TIME, LAST_DAY_END_TIME)
    }

    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                context = mock(),
                logging = mock(),
                executionContext = mock(),
                databaseScope = mock(),
                networkScope = mock(),
                okHttpClient = mock(),
                alarmsDatabaseRepository = mock(),
                highlightsDatabaseRepository = mock(),
                sessionsDatabaseRepository = mock(),
                metaDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                engelsystemNetworkRepository = mock(),
                sharedPreferencesRepository = sharedPreferencesRepository,
                sessionsTransformer = mock()
            )
            return this
        }

    private lateinit var alarmUpdater: AlarmUpdater
    private val mockListener = mock<OnAlarmUpdateListener>()

    @BeforeEach
    fun setUp() {
        alarmUpdater = AlarmUpdater(conferenceTimeFrame, mockListener, testableAppRepository, NoLogging)
    }

    @Test
    fun `calculateInterval schedules alarm with development interval`() {
        whenever(sharedPreferencesRepository.getScheduleRefreshInterval()).doReturn(THREE_SECONDS)
        val interval = alarmUpdater.calculateInterval(LAST_DAY_END_TIME, false)
        assertThat(interval).isEqualTo(THREE_SECONDS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(THREE_SECONDS.toLong(), LAST_DAY_END_TIME.plusSeconds(3))
    }

    @Test
    fun `calculateInterval schedules initial alarm with development interval`() {
        whenever(sharedPreferencesRepository.getScheduleRefreshInterval()).doReturn(THREE_SECONDS)
        val interval = alarmUpdater.calculateInterval(LAST_DAY_END_TIME, true)
        assertThat(interval).isEqualTo(THREE_SECONDS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(THREE_SECONDS.toLong(), LAST_DAY_END_TIME.plusSeconds(3))
    }

    // Start <= Time < End

    @Test
    fun `calculateInterval with time of first day`() {
        // 2015-12-27T11:30:00+0100, in milliseconds: 1451212200000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451212200000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedNever(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time of first day initial`() {
        // 2015-12-27T11:30:00+0100, in milliseconds: 1451212200000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451212200000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        val expectedNextFetch = Moment.ofEpochMilli(1451212200000L).plusMilliseconds(TWO_HOURS)
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, expectedNextFetch)
    }

    // Time == End

    @Test
    fun `calculateInterval with time of last day end time`() {
        // 2015-12-31T00:00:00+0100, in milliseconds: 1451516400000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451516400000L), false)
        assertThat(interval).isEqualTo(0)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time of last day end time initial`() {
        // 2015-12-31T00:00:00+0100, in milliseconds: 1451516400000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451516400000L), true)
        assertThat(interval).isEqualTo(0)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    // Time < Start, diff == 1 second

    @Test
    fun `calculateInterval with time one second before first day`() {
        // 2015-12-26T23:59:59+0100, in milliseconds: 1451170799000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451170799000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451170800000L))
    }

    @Test
    fun `calculateInterval with time one second before first day initial`() {
        // 2015-12-26T23:59:59+0100, in milliseconds: 1451170799000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451170799000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451170800000L))
    }

    // Time < Start, diff == 1 day

    @Test
    fun `calculateInterval with time one day before first day`() {
        // 2015-12-26T00:00:00+0100, in milliseconds: 1451084400000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451084400000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451170800000L))
    }

    @Test
    fun `calculateInterval with time one day before first day initial`() {
        // 2015-12-26T00:00:00+0100, in milliseconds: 1451084400000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451084400000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451170800000L))
    }

    // Time < Start, diff > 1 day

    @Test
    fun `calculateInterval with time more than one day before first day`() {
        // 2015-12-25T23:59:59+0100, in milliseconds: 1451084399000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451084399000L), false)
        assertThat(interval).isEqualTo(ONE_DAY)
        // TODO Is this behavior intended?
        verifyInvokedNever(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time more than one day before first day initial`() {
        // 2015-12-25T23:59:59+0100, in milliseconds: 1451084399000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451084399000L), true)
        assertThat(interval).isEqualTo(ONE_DAY)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        val expectedNextFetch = Moment.ofEpochMilli(1451084399000L).plusMilliseconds(ONE_DAY)
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(ONE_DAY, expectedNextFetch)
    }

}
