package nerd.tuxmobil.fahrplan.congress.search.filters

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class NotRecordedSearchFilter : SearchFilter {
    override val label = R.string.search_filter_not_recorded

    override fun isMatch(session: Session, query: String): Boolean {
        return session.recordingOptOut
    }
}
