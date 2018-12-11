package nerd.tuxmobil.fahrplan.congress.navigation;

import android.support.annotation.NonNull;
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
        put("ASSEMBLY:CHAOS WEST", "chaos-west-stage");
        put("ASSEMBLY:BOGONAUTEN", null);
        put("ASSEMBLY:HAECKSEN", null);
        put("ASSEMBLY:HARDWAREHACKINGAREA", "hardware-hacking-area");
        put("ASSEMBLY:JUGEND HACKT", "jugend-hackt");
        put("ASSEMBLY:MILLIWAYS", "milliways");
        put("ASSEMBLY:OPEN KNOWLEDGE ASSEMBLY", null);
        put("ASSEMBLY:PHYSIKFACHSCHAFT ROSTOCK", null);
        put("ASSEMBLY:TEAHOUSE", null);
        put("CCL HALL 3", "ccl-hall-3");
        put("CHAOS WEST STAGE", "chaos-west-stage");
        put("HALL 3", "ccl-hall-3");
        put("HIVE STAGE", "the-hive-stage");
        put("KIDSPACE", "kidspace");
        put("KOMONA AQUARIUS", "komona-aquarius");
        put("KOMONA BLUE PRINCESS", "komona-blue-princess");
        put("KOMONA CORAL REEF", "komona-coral-reef");
        put("KOMONA D.RESSROSA", "komona-d-ressrosa");
        put("LECTURE ROOM 11", "ccl-lecture-room-11");
        put("LECTURE ROOM 12", "ccl-lecture-room-12");
        put("SEMINAR ROOM 13", "ccl-seminar-room-13");
        put("SEMINAR ROOM 14-15", "ccl-seminar-room-13");
    }};

    @NonNull
    public static String convert(@Nullable final String room) {
        if (room != null && !EMPTY_STRING.equals(room)) {
            String c3navName = ROOM_TO_C3NAV_MAPPING.get(room.toUpperCase());
            return c3navName == null ? EMPTY_STRING : c3navName;
        }
        return EMPTY_STRING;
    }

}
