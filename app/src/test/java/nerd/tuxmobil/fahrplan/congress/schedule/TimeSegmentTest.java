package nerd.tuxmobil.fahrplan.congress.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.TimeZone;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

import static info.metadude.android.eventfahrplan.commons.temporal.Moment.MILLISECONDS_OF_ONE_MINUTE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class TimeSegmentTest {

    @Test
    public void isMatched() {
        int minute = 25;
        int msOfMinute = (int) (minute * MILLISECONDS_OF_ONE_MINUTE);
        TimeSegment segment = new TimeSegment(minute);
        assertThat(segment.isMatched(Moment.ofEpochMilli(msOfMinute), 15)).isEqualTo(true);
    }
}
