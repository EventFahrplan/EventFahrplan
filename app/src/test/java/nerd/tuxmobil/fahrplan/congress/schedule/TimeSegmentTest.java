package nerd.tuxmobil.fahrplan.congress.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.TimeZone;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class TimeSegmentTest {

    @Test
    public void getFormattedTextWith0() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        TimeSegment segment = new TimeSegment(0);
        assertThat(segment.getFormattedText()).isEqualTo("01:00");
    }

    @Test
    public void getFormattedTextWith120() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        TimeSegment segment = new TimeSegment(120);
        assertThat(segment.getFormattedText()).isEqualTo("03:00");
    }

    @Test
    public void getFormattedTextWith660() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        TimeSegment segment = new TimeSegment(660);
        assertThat(segment.getFormattedText()).isEqualTo("12:00");
    }

    @Test
    public void getFormattedTextWith1425() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        TimeSegment segment = new TimeSegment(1425);
        assertThat(segment.getFormattedText()).isEqualTo("00:45");
    }

    @Test
    public void isMatched() {
        int minute = 25;
        int msOfMinute = minute * 60 * 1000;
        TimeSegment segment = new TimeSegment(minute);
        assertThat(segment.isMatched(new Moment(msOfMinute), 15)).isEqualTo(true);
    }
}
