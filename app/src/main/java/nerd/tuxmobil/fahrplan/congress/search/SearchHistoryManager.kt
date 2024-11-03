package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchHistoryManager(
    private val repository: SearchRepository,
) {

    private companion object {
        const val MAX_HISTORY_SIZE = 20
    }

    val searchHistory: Flow<List<String>> = repository.searchHistory
        .map { it.filter(String::isNotEmpty) }
        .map { it.reversed() }

    fun append(scope: CoroutineScope, query: String) {
        scope.launch {
            val history = repository.searchHistory.first().toMutableList()
            do {
                val found = history.remove(query)
            } while (found)
            history.add(query)
            if (history.size > MAX_HISTORY_SIZE) {
                history.removeAt(0)
            }
            repository.updateSearchHistory(history)
        }
    }

    fun clear(scope: CoroutineScope) {
        scope.launch {
            repository.updateSearchHistory(emptyList())
        }
    }

}
