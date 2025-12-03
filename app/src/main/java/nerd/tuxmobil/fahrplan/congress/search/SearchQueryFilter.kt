package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.models.Session

class SearchQueryFilter {

    /**
     * Filters all text fields of a session which are visible to the user, e.g. in the details screen.
     */
    fun List<Session>.filterAll(query: String): List<Session> = filter {
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

    fun filterFavored(sessions: List<Session>): List<Session> = sessions.filter {
        it.isHighlight
    }
}

fun List<Session>.filterAll(query: String): List<Session> = filter {
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

fun List<Session>.filterFavored(): List<Session> = filter {
    it.isHighlight
}

fun List<Session>.filterNotRecorded(): List<Session> = filter {
    it.recordingOptOut
}
