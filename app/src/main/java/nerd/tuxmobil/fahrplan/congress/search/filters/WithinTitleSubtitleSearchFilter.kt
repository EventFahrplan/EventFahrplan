package nerd.tuxmobil.fahrplan.congress.search.filters

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class WithinTitleSubtitleSearchFilter : SearchFilter {
    override val label = R.string.search_filter_within_title_subtitle

    override fun isMatch(session: Session, query: String): Boolean {
        if (query.isEmpty()) return false

        return session.title.contains(query, ignoreCase = true) ||
            session.subtitle.contains(query, ignoreCase = true)
    }
}
