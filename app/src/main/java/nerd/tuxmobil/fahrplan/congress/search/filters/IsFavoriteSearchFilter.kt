package nerd.tuxmobil.fahrplan.congress.search.filters

import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class IsFavoriteSearchFilter : SearchFilter {
    override fun isMatch(session: Session, query: String): Boolean {
        return session.isHighlight
    }
}
