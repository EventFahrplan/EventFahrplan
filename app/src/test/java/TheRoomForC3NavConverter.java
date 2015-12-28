import org.junit.Test;

import nerd.tuxmobil.fahrplan.congress.RoomForC3NavConverter;

import static org.assertj.core.api.Assertions.assertThat;

public class TheRoomForC3NavConverter {

    @Test
    public void testThatHall6Works() {
        assertThat(RoomForC3NavConverter.convert("cch", "HALL 6")).isEqualTo("h6");
    }


    @Test
    public void testThatHallGWorks() {
        assertThat(RoomForC3NavConverter.convert("cch", "HALL G")).isEqualTo("hg");
    }
}
