package nerd.tuxmobil.fahrplan.congress.navigation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RoomForC3NavConverterTest {

    @Test
    public void convertWithSaalAdams() {
        assertThat(RoomForC3NavConverter.convert("SAAL ADAMS")).isEqualTo("hall-a");
    }

    @Test
    public void convertWithSaalBorg() {
        assertThat(RoomForC3NavConverter.convert("SAAL BORG")).isEqualTo("hall-b");
    }

    @Test
    public void convertWithSaalClarke() {
        assertThat(RoomForC3NavConverter.convert("SAAL CLARKE")).isEqualTo("hall-c");
    }

    @Test
    public void convertWithSaalDijkstra() {
        assertThat(RoomForC3NavConverter.convert("SAAL DIJKSTRA")).isEqualTo("hall-d");
    }

}
