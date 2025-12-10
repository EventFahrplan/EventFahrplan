package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySearchRepository(
    initialValue: List<String> = emptyList(),
) : SearchRepository {
    private val mutableHistory = MutableStateFlow(initialValue)

    override val searchHistory: Flow<List<String>> = mutableHistory.asStateFlow()

    override fun updateSearchHistory(history: List<String>) {
        mutableHistory.value = history
    }
}
