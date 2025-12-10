package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.models.Session

interface SearchResultParameterFactory {
    fun createSearchResults(
        sessions: List<Session>,
        useDeviceTimeZone: Boolean,
    ): List<SearchResultParameter>
}
