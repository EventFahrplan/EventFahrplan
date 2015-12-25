package nerd.tuxmobil.fahrplan.congress;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RoomForC3NavConverter {

    private final static Map<String,String> CCH_MAP= new HashMap<String, String>() {{
        put("HALL 1","h1");
        put("HALL 2","h2");
        put("HALL 3","h3");
        put("HALL 6","h6");
        put("HALL 13","h13");
        put("HALL 14","h14");

        put("HALL B","hb");
        put("HALL G","hg");
        put("HALL F","hf");
    }};

    @Nullable
    public static String convert(final String venue, final String room) {

        if (venue.toUpperCase().equals("CCH")) {
            return CCH_MAP.get(room.toUpperCase());
        }

        return null;
    }

}
