package nerd.tuxmobil.fahrplan.congress.wiki;

import android.support.annotation.NonNull;

public class WikiEventUtils {

    private static final String WIKI_LINK_PREFIX =
            "https://events.ccc.de/congress/2017/wiki/index.php/Session:";

    /**
     * Returns true if the given links contain a link to the
     * corresponding wiki session; Otherwise false.
     *
     * Background information: Self-organized sessions are exported
     * from the CCC wiki. The export file then contains regular events
     * plus wiki events. For wiki events the valid link to the wiki
     * is contained in the links whereas for regular events the link
     * is generated based on the event id.
     * Export script: https://github.com/voc/schedule
     */
    public static boolean linksContainWikiLink(@NonNull String links) {
        return links.contains(WIKI_LINK_PREFIX);
    }

}
