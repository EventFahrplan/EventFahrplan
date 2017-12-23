package nerd.tuxmobil.fahrplan.congress.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class TimeSegmentTest {

    @Test
    public void getFormattedTextWith0() throws Exception {
        TimeSegment segment = new TimeSegment(0);
        assertThat(segment.getFormattedText()).isEqualTo("00:00");
    }

    @Test
    public void getFormattedTextWith120() throws Exception {
        TimeSegment segment = new TimeSegment(120);
        assertThat(segment.getFormattedText()).isEqualTo("02:00");
    }

    @Test
    public void getFormattedTextWith660() throws Exception {
        TimeSegment segment = new TimeSegment(660);
        assertThat(segment.getFormattedText()).isEqualTo("11:00");
    }

    @Test
    public void getFormattedTextWith1425() throws Exception {
        TimeSegment segment = new TimeSegment(1425);
        assertThat(segment.getFormattedText()).isEqualTo("23:45");
    }

}
