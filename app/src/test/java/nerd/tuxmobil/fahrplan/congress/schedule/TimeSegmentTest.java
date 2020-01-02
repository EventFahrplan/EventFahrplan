package nerd.tuxmobil.fahrplan.congress.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class TimeSegmentTest {

    @Test
    public void getFormattedTextWith0() {
        TimeSegment segment = new TimeSegment(0);
        assertThat(segment.getFormattedText()).isEqualTo("00:00");
    }

    @Test
    public void getFormattedTextWith120() {
        TimeSegment segment = new TimeSegment(120);
        assertThat(segment.getFormattedText()).isEqualTo("02:00");
    }

    @Test
    public void getFormattedTextWith660() {
        TimeSegment segment = new TimeSegment(660);
        assertThat(segment.getFormattedText()).isEqualTo("11:00");
    }

    @Test
    public void getFormattedTextWith1425() {
        TimeSegment segment = new TimeSegment(1425);
        assertThat(segment.getFormattedText()).isEqualTo("23:45");
    }

    @Test
    public void isMatched() {
        int minute = 25;
        int msOfMinute = minute * 60 * 1000;
        TimeSegment segment = new TimeSegment(minute);
        // FIXME fails because Moment.getHour returns 1, because "UTC hour 0 plus 1 hour zone offset". Segment is not zone aware and returns hour = 0
        // I think all date arithmetic should be done in UTC, only displayed values should be zoned.
        assertThat(segment.isMatched(new Moment(msOfMinute), 15)).isEqualTo(true);
    }
}
