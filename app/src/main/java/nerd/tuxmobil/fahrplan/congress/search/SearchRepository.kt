package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    val searchHistory: Flow<List<String>>
    fun updateSearchHistory(history: List<String>)
}
