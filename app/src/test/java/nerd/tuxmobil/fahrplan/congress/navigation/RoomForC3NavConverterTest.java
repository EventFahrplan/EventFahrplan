package nerd.tuxmobil.fahrplan.congress.navigation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RoomForC3NavConverterTest {

    @Test
    public void testThatHall6Works() {
        assertThat(RoomForC3NavConverter.convert("HALL 6")).isEqualTo("h6");
    }


    @Test
    public void testThatHallGWorks() {
        assertThat(RoomForC3NavConverter.convert("HALL G")).isEqualTo("hg");
    }
}
