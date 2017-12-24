package nerd.tuxmobil.fahrplan.congress.navigation;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RoomForC3NavConverter {

    private static final String EMPTY_STRING = "";

    private static final Map<String, String> ROOM_TO_C3NAV_MAPPING = new HashMap<String, String>() {{
        put("SAAL ADAMS", "hall-a");
        put("SAAL BORG", "hall-b");
        put("SAAL CLARKE", "hall-c");
        put("SAAL DIJKSTRA", "hall-d");

        // From everything.schedule.xml
        put("Assembly:Chaos West", "chaos-west-stage");
        put("Assembly:Bogonauten", null);
        put("Assembly:Haecksen", null);
        put("Assembly:HardwareHackingArea", "hardware-hacking-area");
        put("Assembly:Jugend hackt", "jugend-hackt");
        put("Assembly:Milliways", "milliways");
        put("Assembly:Open Knowledge Assembly", null);
        put("Assembly:Physikfachschaft Rostock", null);
        put("Assembly:TeaHouse", null);
        put("CCL Hall 3", "ccl-hall-3");
        put("Chaos West Stage", "chaos-west-stage");
        put("Hall 3", "ccl-hall-3");
        put("Hive Stage", "the-hive-stage");
        put("Kidspace", "kidspace");
        put("Komona Aquarius", "komona-aquarius");
        put("Komona Blue Princess", "komona-blue-princess");
        put("Komona Coral Reef", "komona-coral-reef");
        put("Komona D.Ressrosa", "komona-d-ressrosa");
        put("Lecture room 11", "ccl-lecture-room-11");
        put("Lecture room 12", "ccl-lecture-room-12");
        put("Seminar room 13", "ccl-seminar-room-13");
        put("Seminar room 14-15", "ccl-seminar-room-13");
    }};

    @Nullable
    public static String convert(@Nullable final String room) {
        if (room != null && !EMPTY_STRING.equals(room)) {
            return ROOM_TO_C3NAV_MAPPING.get(room.toUpperCase());
        }
        return null;
    }

}
