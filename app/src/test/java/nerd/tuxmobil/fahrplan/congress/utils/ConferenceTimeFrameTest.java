package nerd.tuxmobil.fahrplan.congress.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ConferenceTimeFrameTest {

    // 2015-12-27T00:00:00+0100, in seconds: 1451170800000
    private static final long FIRST_DAY_START_TIME = 1451170800000L;

    // 2015-12-31T00:00:00+0100, in seconds: 1451516400000
    private static final long LAST_DAY_END_TIME = 1451516400000L;

    private ConferenceTimeFrame conference;

    @Before
    public void setUp() {
        conference = new ConferenceTimeFrame(FIRST_DAY_START_TIME, LAST_DAY_END_TIME);
    }

    @Test
    public void getFirstDayStart() {
        assertThat(conference.getFirstDayStartTime()).isEqualTo(FIRST_DAY_START_TIME);
    }

    @Test
    public void containsWithTimeWithFirstDayEvent() {
        // 2015-12-27T11:30:00+0100, in seconds: 1451212200000
        assertThat(conference.contains(1451212200000L)).isTrue();
    }

    @Test
    public void containsWithOneSecondBeforeFirstDay() {
        // 2015-12-26T23:59:59+0100, in seconds: 1451170799000
        assertThat(conference.contains(1451170799000L)).isFalse();
    }

    @Test
    public void containsWithTimeOfLastDay() {
        assertThat(conference.contains(LAST_DAY_END_TIME)).isFalse();
    }

    @Test
    public void endsBeforeWithTimeOfLastDay() {
        assertThat(conference.endsBefore(LAST_DAY_END_TIME)).isTrue();
    }

    @Test
    public void endsBeforeWithTimeBeforeLastDay() {
        assertThat(conference.endsBefore(LAST_DAY_END_TIME - 1)).isFalse();
    }

    @Test
    public void startsAfterWithTimeOfFirstDay() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME)).isFalse();
    }

    @Test
    public void startsAfterWithTimeBeforeFirstDay() {
        assertThat(conference.startsAfter(FIRST_DAY_START_TIME - 1)).isTrue();
    }

    @Test
    public void startsAtOrBeforeWithTimeOfFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME)).isTrue();
    }

    @Test
    public void startsAtOrBeforeWithTimeAfterFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME + 1)).isTrue();
    }

    @Test
    public void startsAtOrBeforeWithTimeBeforeFirstDay() {
        assertThat(conference.startsAtOrBefore(FIRST_DAY_START_TIME - 1)).isFalse();
    }

}
