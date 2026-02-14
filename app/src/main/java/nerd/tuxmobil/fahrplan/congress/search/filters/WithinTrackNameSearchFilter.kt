package nerd.tuxmobil.fahrplan.congress.search.filters

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class WithinTrackNameSearchFilter : SearchFilter {
    override val label = R.string.search_filter_within_track_name

    override fun isMatch(session: Session, query: String): Boolean {
        if (query.isEmpty()) return false

        return session.track.contains(query, ignoreCase = true)
    }
}
