package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.models.Session

class SearchQueryFilter {

    /**
     * Filters all text fields of a session which are visible to the user, e.g. in the details screen.
     */
    fun filterAll(sessions: List<Session>, query: String): List<Session> = sessions.filter {
        it.sessionId.contains(query, ignoreCase = true)
                || it.title.contains(query, ignoreCase = true)
                || it.subtitle.contains(query, ignoreCase = true)
                || it.abstractt.contains(query, ignoreCase = true)
                || it.description.contains(query, ignoreCase = true)
                || it.track.contains(query, ignoreCase = true)
                || it.roomName.contains(query, ignoreCase = true)
                || it.links.contains(query, ignoreCase = true)
                || it.speakers.any { name -> name.contains(query, ignoreCase = true) }
    }

    fun filterAll(sessions: List<Session>, query: String, filters: Set<SearchFilter>): List<Session> {
        val results = if (query.isEmpty()) sessions else filterAll(sessions, query)
        return results.filter { session ->
            filters.all { filter -> filter.isMatch(session, query) }
        }
    }
}
