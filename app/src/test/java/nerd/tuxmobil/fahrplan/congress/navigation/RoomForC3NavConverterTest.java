package nerd.tuxmobil.fahrplan.congress.navigation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RoomForC3NavConverterTest {

    @Test
    public void convertWithAdams() {
        assertThat(RoomForC3NavConverter.convert("Adams")).isEqualTo("hall-a");
    }

    @Test
    public void convertWithBorg() {
        assertThat(RoomForC3NavConverter.convert("Borg")).isEqualTo("hall-b");
    }

    @Test
    public void convertWithClarke() {
        assertThat(RoomForC3NavConverter.convert("Clarke")).isEqualTo("hall-c");
    }

    @Test
    public void convertWithDijkstra() {
        assertThat(RoomForC3NavConverter.convert("Dijkstra")).isEqualTo("hall-d");
    }

    @Test
    public void convertWithEliza() {
        assertThat(RoomForC3NavConverter.convert("Eliza")).isEqualTo("hall-e");
    }

    @Test
    public void convertWithNonExisting() {
        assertThat(RoomForC3NavConverter.convert("NonExisting")).isEmpty();
    }

    @Test
    public void convertWithNull() {
        assertThat(RoomForC3NavConverter.convert(null)).isEmpty();
    }

}
