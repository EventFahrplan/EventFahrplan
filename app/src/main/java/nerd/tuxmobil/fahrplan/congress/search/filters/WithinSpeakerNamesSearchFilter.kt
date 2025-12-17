package nerd.tuxmobil.fahrplan.congress.search.filters

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.SearchFilter

class WithinSpeakerNamesSearchFilter : SearchFilter {
    override val label = R.string.search_filter_within_speaker_names

    override fun isMatch(session: Session, query: String): Boolean {
        if (query.isEmpty()) return false

        return session.speakers.any { speakerName ->
            speakerName.contains(query, ignoreCase = true)
        }
    }
}
