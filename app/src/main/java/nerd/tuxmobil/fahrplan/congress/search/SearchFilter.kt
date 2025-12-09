package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.models.Session

interface SearchFilter {
    fun isMatch(session: Session, query: String): Boolean
}
