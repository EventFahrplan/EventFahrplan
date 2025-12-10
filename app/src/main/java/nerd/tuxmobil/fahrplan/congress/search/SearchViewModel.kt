package nerd.tuxmobil.fahrplan.congress.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.NoSearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchHistory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: AppRepository,
    private val searchQueryFilter: SearchQueryFilter,
    private val searchHistoryManager: SearchHistoryManager,
    private val searchResultParameterFactory: SearchResultParameterFactory,
) : ViewModel() {

    private companion object {
        const val FINISH_TYPING_SEARCH_QUERY_DELAY = 1_000L
    }

    private val mutableEffects = Channel<SearchEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    val uiState: StateFlow<SearchUiState> =
        combine(
            searchQuery,
            repository.sessions,
            searchHistoryManager.searchHistory,
        ) { query, sessions, searchHistory ->
            val resultsState = if (query.isEmpty()) {
                if (searchHistory.isEmpty()) {
                    NoSearchResults(backEvent = OnBackPress)
                } else {
                    SearchHistory(searchTerms = searchHistory.toImmutableList())
                }
            } else {
                val matchingSessions = searchQueryFilter.filterAll(sessions, query)
                if (matchingSessions.isEmpty()) {
                    NoSearchResults(backEvent = OnSearchSubScreenBackPress)
                } else {
                    SearchResults(
                        searchResults = searchResultParameterFactory.createSearchResults(
                            matchingSessions,
                            useDeviceTimeZone,
                        ).toImmutableList()
                    )
                }
            }

            SearchUiState(
                query = query,
                resultsState = resultsState,
            )
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = SearchUiState(),
            started = WhileSubscribed(5_000)
        )

    init {
        searchQuery
            .debounce(FINISH_TYPING_SEARCH_QUERY_DELAY)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .onEach { searchHistoryManager.append(viewModelScope, it) }
            .launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: SearchViewEvent) {
        when (viewEvent) {
            OnBackPress -> sendEffect(NavigateBack)
            OnBackIconClick -> searchQuery.value = ""
            OnSearchSubScreenBackPress -> searchQuery.value = ""
            is OnSearchResultItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
            is OnSearchHistoryItemClick -> searchQuery.value = viewEvent.searchQuery
            OnSearchHistoryClear -> searchHistoryManager.clear(viewModelScope)
            is OnSearchQueryChange -> searchQuery.value = viewEvent.updatedQuery
            OnSearchQueryClear -> searchQuery.value = ""
        }
    }

    private fun sendEffect(effect: SearchEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }
}
