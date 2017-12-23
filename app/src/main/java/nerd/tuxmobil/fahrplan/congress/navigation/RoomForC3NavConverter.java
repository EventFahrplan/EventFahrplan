package nerd.tuxmobil.fahrplan.congress.navigation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RoomForC3NavConverter {

    private static final String EMPTY_STRING = "";

    private static final Map<String, String> ROOM_TO_C3NAV_MAPPING = new HashMap<String, String>() {{
        put("HALL 1", "h1");
        put("HALL 2", "h2");
        put("HALL 3", "h3");
        put("HALL 6", "h6");
        put("HALL 13", "h13");
        put("HALL 14", "h14");

        put("HALL B", "hb");
        put("HALL G", "hg");
        put("HALL F", "hf");
    }};

    @Nullable
    public static String convert(@NonNull final String venue, @Nullable final String room) {
        if (room != null && !EMPTY_STRING.equals(room) && venue.toUpperCase().equals("CCH")) {
            return ROOM_TO_C3NAV_MAPPING.get(room.toUpperCase());
        }
        return null;
    }

}
