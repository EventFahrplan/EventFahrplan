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
        put("Assembly:Chaos West", null);
        put("Assembly:Bogonauten", null);
        put("Assembly:Haecksen", null);
        put("Assembly:HardwareHackingArea", null);
        put("Assembly:Jugend hackt", null);
        put("Assembly:Milliways", null);
        put("Assembly:Open Knowledge Assembly", null);
        put("Assembly:Physikfachschaft Rostock", null);
        put("Assembly:TeaHouse", null);
        put("CCL Hall 3", null);
        put("Chaos West Stage", null);
        put("Hall 3", null);
        put("Hive Stage", null);
        put("Kidspace", null);
        put("Komona Aquarius", null);
        put("Komona Blue Princess", null);
        put("Komona Coral Reef", null);
        put("Komona D.Ressrosa", null);
        put("Lecture room 11", null);
        put("Lecture room 12", null);
        put("Seminar room 13", null);
        put("Seminar room 14-15", null);
    }};

    @Nullable
    public static String convert(@Nullable final String room) {
        if (room != null && !EMPTY_STRING.equals(room)) {
            return ROOM_TO_C3NAV_MAPPING.get(room.toUpperCase());
        }
        return null;
    }

}
