package nerd.tuxmobil.fahrplan.congress.alarms;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(JUnit4.class)
public class AlarmUpdaterTest {

    // 2015-12-27T00:00:00+0100, in seconds: 1451170800000
    final long FIRST_DAY_START_TIME = 1451170800000L;

    // 2015-12-31T00:00:00+0100, in seconds: 1451516400000
    final long LAST_DAY_END_TIME = 1451516400000L;

    final ConferenceTimeFrame conferenceTimeFrame =
            new ConferenceTimeFrame(FIRST_DAY_START_TIME, LAST_DAY_END_TIME);

    final long NEVER_USED = -1;

    AlarmUpdater alarmUpdater;

    AlarmUpdater.OnAlarmUpdateListener mockListener =
            mock(AlarmUpdater.OnAlarmUpdateListener.class);

    @Before
    public void setUp() throws Exception {
        alarmUpdater = new AlarmUpdater(conferenceTimeFrame, mockListener);
    }

    // Start <= Time < End

    @Test
    public void calculateIntervalWithTimeOfFirstDay() throws Exception {
        // 2015-12-27T11:30:00+0100, in seconds: 1451212200000
        long interval = alarmUpdater.calculateInterval(1451212200000L, false);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener, never()).onCancelAlarm();
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    @Test
    public void calculateIntervalWithTimeOfFirstDayInitial() throws Exception {
        // 2015-12-27T11:30:00+0100, in seconds: 1451212200000
        long interval = alarmUpdater.calculateInterval(1451212200000L, true);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleInitialAlarm(7200000L, 1451212200000L + 7200000L);
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
    }

    // Time == End

    @Test
    public void calculateIntervalWithTimeOfLastDayEndTime() throws Exception {
        // 2015-12-31T00:00:00+0100, in seconds: 1451516400000
        long interval = alarmUpdater.calculateInterval(1451516400000L, false);
        assertThat(interval).isEqualTo(0);
        verify(mockListener).onCancelAlarm();
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    @Test
    public void calculateIntervalWithTimeOfLastDayEndTimeInitial() throws Exception {
        // 2015-12-31T00:00:00+0100, in seconds: 1451516400000
        long interval = alarmUpdater.calculateInterval(1451516400000L, true);
        assertThat(interval).isEqualTo(0);
        verify(mockListener).onCancelAlarm();
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    // Time < Start, diff == 1 second

    @Test
    public void calculateIntervalWithTimeOneSecondBeforeFirstDay() throws Exception {
        // 2015-12-26T23:59:59+0100, in seconds: 1451170799000
        long interval = alarmUpdater.calculateInterval(1451170799000L, false);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleAlarm(7200000L, 1451170800000L);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    @Test
    public void calculateIntervalWithTimeOneSecondBeforeFirstDayInitial() throws Exception {
        // 2015-12-26T23:59:59+0100, in seconds: 1451170799000
        long interval = alarmUpdater.calculateInterval(1451170799000L, true);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleInitialAlarm(7200000L, 1451170800000L);
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
    }

    // Time < Start, diff == 1 day

    @Test
    public void calculateIntervalWithTimeOneDayBeforeFirstDay() throws Exception {
        // 2015-12-26T00:00:00+0100, in seconds: 1451084400000
        long interval = alarmUpdater.calculateInterval(1451084400000L, false);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleAlarm(7200000L, 1451170800000L);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    @Test
    public void calculateIntervalWithTimeOneDayBeforeFirstDayInitial() throws Exception {
        // 2015-12-26T00:00:00+0100, in seconds: 1451084400000
        long interval = alarmUpdater.calculateInterval(1451084400000L, true);
        assertThat(interval).isEqualTo(7200000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleInitialAlarm(7200000L, 1451170800000L);
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
    }

    // Time < Start, diff > 1 day

    @Test
    public void calculateIntervalWithTimeMoreThanOneDayBeforeFirstDay() throws Exception {
        // 2015-12-25T23:59:59+0100, in seconds: 1451084399000
        long interval = alarmUpdater.calculateInterval(1451084399000L, false);
        assertThat(interval).isEqualTo(86400000L);
        // TODO Is this behavior intended?
        verify(mockListener, never()).onCancelAlarm();
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
        verify(mockListener, never()).onRescheduleInitialAlarm(NEVER_USED, NEVER_USED);
    }

    @Test
    public void calculateIntervalWithTimeMoreThanOneDayBeforeFirstDayInitial() throws Exception {
        // 2015-12-25T23:59:59+0100, in seconds: 1451084399000
        long interval = alarmUpdater.calculateInterval(1451084399000L, true);
        assertThat(interval).isEqualTo(86400000L);
        verify(mockListener).onCancelAlarm();
        verify(mockListener).onRescheduleInitialAlarm(86400000L, 1451084399000L + 86400000L);
        verify(mockListener, never()).onRescheduleAlarm(NEVER_USED, NEVER_USED);
    }

}
