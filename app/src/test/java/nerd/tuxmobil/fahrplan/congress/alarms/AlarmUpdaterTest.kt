package nerd.tuxmobil.fahrplan.congress.alarms

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater.OnAlarmUpdateListener
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Unknown
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AlarmUpdaterTest {

    private companion object {

        // 2015-12-27T00:00:00Z, in milliseconds: 1451174400000
        val FIRST_DAY_START_TIME = Moment.ofEpochMilli(1451174400000L)

        // 2015-12-31T00:00:00Z, in milliseconds: 1451520000000
        val LAST_DAY_END_TIME = Moment.ofEpochMilli(1451520000000L)

        const val NEVER_USED: Long = -1
        val NEVER_USED_MOMENT: Moment = Moment.ofEpochMilli(NEVER_USED)

        const val THREE_SECONDS = 3 * Moment.MILLISECONDS_OF_ONE_SECOND
        const val TWO_HOURS = 2 * Moment.MILLISECONDS_OF_ONE_HOUR
        const val ONE_DAY = Moment.MILLISECONDS_OF_ONE_DAY

        val conferenceTimeFrame = ConferenceTimeFrame.Known(FIRST_DAY_START_TIME, LAST_DAY_END_TIME)
    }

    private val settingsRepository = mock<SettingsRepository>()

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
                engelsystemRepository = mock(),
                sharedPreferencesRepository = mock(),
                settingsRepository = settingsRepository,
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

    // Developer interval

    @Test
    fun `calculateInterval schedules alarm with development interval`() {
        whenever(settingsRepository.getScheduleRefreshInterval()).doReturn(THREE_SECONDS)
        val interval = alarmUpdater.calculateInterval(LAST_DAY_END_TIME, false)
        assertThat(interval).isEqualTo(THREE_SECONDS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(THREE_SECONDS.toLong(), LAST_DAY_END_TIME.plusSeconds(3))
    }

    @Test
    fun `calculateInterval schedules initial alarm with development interval`() {
        whenever(settingsRepository.getScheduleRefreshInterval()).doReturn(THREE_SECONDS)
        val interval = alarmUpdater.calculateInterval(LAST_DAY_END_TIME, true)
        assertThat(interval).isEqualTo(THREE_SECONDS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(THREE_SECONDS.toLong(), LAST_DAY_END_TIME.plusSeconds(3))
    }

    // Unknown

    @Test
    fun `calculateInterval schedules alarm with unknown conference time frame`() {
        val alarmUpdater = AlarmUpdater(Unknown, mockListener, testableAppRepository, NoLogging)
        val interval = alarmUpdater.calculateInterval(LAST_DAY_END_TIME, false)
        assertThat(interval).isEqualTo(ONE_DAY)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(ONE_DAY, LAST_DAY_END_TIME.plusMilliseconds(ONE_DAY))
    }

    // Start <= Time < End

    @Test
    fun `calculateInterval with time of first day`() {
        // 2015-12-27T11:30:00Z, in milliseconds: 1451215800000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451215800000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedNever(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time of first day initial`() {
        // 2015-12-27T11:30:00Z, in milliseconds: 1451215800000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451215800000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        val expectedNextFetch = Moment.ofEpochMilli(1451215800000L).plusMilliseconds(TWO_HOURS)
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, expectedNextFetch)
    }

    // Time == End

    @Test
    fun `calculateInterval with time of last day end time`() {
        // 2015-12-31T00:00:00Z, in milliseconds: 1451520000000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451520000000L), false)
        assertThat(interval).isEqualTo(0)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time of last day end time initial`() {
        // 2015-12-31T00:00:00Z, in milliseconds: 1451520000000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451520000000L), true)
        assertThat(interval).isEqualTo(0)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    // Time < Start, diff == 1 second

    @Test
    fun `calculateInterval with time one second before first day`() {
        // 2015-12-26T23:59:59Z, in milliseconds: 1451174399000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451174399000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451174400000L))
    }

    @Test
    fun `calculateInterval with time one second before first day initial`() {
        // 2015-12-26T23:59:59Z, in milliseconds: 1451174399000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451174399000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451174400000L))
    }

    // Time < Start, diff == 1 day

    @Test
    fun `calculateInterval with time one day before first day`() {
        // 2015-12-26T00:00:00Z, in milliseconds: 1451088000000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451088000000L), false)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451174400000L))
    }

    @Test
    fun `calculateInterval with time one day before first day initial`() {
        // 2015-12-26T00:00:00Z, in milliseconds: 1451088000000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451088000000L), true)
        assertThat(interval).isEqualTo(TWO_HOURS)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(TWO_HOURS, Moment.ofEpochMilli(1451174400000L))
    }

    // Time < Start, diff > 1 day

    @Test
    fun `calculateInterval with time more than one day before first day`() {
        // 2015-12-25T23:59:59Z, in milliseconds: 1451087999000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451087999000L), false)
        assertThat(interval).isEqualTo(ONE_DAY)
        // TODO Is this behavior intended?
        verifyInvokedNever(mockListener).onCancelUpdateAlarm()
        verifyInvokedNever(mockListener).onScheduleUpdateAlarm(NEVER_USED, NEVER_USED_MOMENT)
    }

    @Test
    fun `calculateInterval with time more than one day before first day initial`() {
        // 2015-12-25T23:59:59Z, in milliseconds: 1451087999000
        val interval = alarmUpdater.calculateInterval(Moment.ofEpochMilli(1451087999000L), true)
        assertThat(interval).isEqualTo(ONE_DAY)
        verifyInvokedOnce(mockListener).onCancelUpdateAlarm()
        val expectedNextFetch = Moment.ofEpochMilli(1451087999000L).plusMilliseconds(ONE_DAY)
        verifyInvokedOnce(mockListener).onScheduleUpdateAlarm(ONE_DAY, expectedNextFetch)
    }

}
