package nerd.tuxmobil.fahrplan.congress.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.search.SearchEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.NoSearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchHistory
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.SearchResults
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnFilterToggled
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotRecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.RecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinSpeakerNamesSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTitleSubtitleSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTrackNameSearchFilter
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: AppRepository,
    private val searchQueryFilter: SearchQueryFilter,
    private val searchHistoryManager: SearchHistoryManager,
    private val searchResultParameterFactory: SearchResultParameterFactory,
    searchFilters: List<SearchFilter> = SUPPORTED_SEARCH_FILTERS,
) : ViewModel() {

    private companion object {
        val FINISH_TYPING_SEARCH_QUERY_DELAY = 1_000.milliseconds

        private val SUPPORTED_SEARCH_FILTERS = listOf(
            IsFavoriteSearchFilter(),
            NotFavoriteSearchFilter(),
            HasAlarmSearchFilter(),
            NotRecordedSearchFilter(),
            RecordedSearchFilter(),
            WithinSpeakerNamesSearchFilter(),
            WithinTitleSubtitleSearchFilter(),
            WithinTrackNameSearchFilter(),
        )
    }

    private val mutableEffects = Channel<SearchEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    private val searchFiltersState = MutableStateFlow(
        SearchFiltersState(
            filters = searchFilters.associateWith { false },
            selectedFilterLabels = emptyList(),
        )
    )

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    val uiState: StateFlow<SearchUiState> =
        combine(
            searchQuery,
            searchFiltersState,
            repository.sessions,
            searchHistoryManager.searchHistory,
        ) { query, searchFiltersState, sessions, searchHistory ->
            val filters = searchFiltersState.filters
            val activeFilters = filters
                .asSequence()
                .filter { it.value }
                .map { it.key }
                .toSet()

            val resultsState = if (query.isEmpty() && activeFilters.isEmpty()) {
                if (searchHistory.isEmpty()) {
                    NoSearchResults(backEvent = OnBackPress)
                } else {
                    SearchHistory(searchTerms = searchHistory.toImmutableList())
                }
            } else {
                val matchingSessions = searchQueryFilter.filterAll(sessions, query, activeFilters)
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
                filters = filters.toUiState(),
                resultsState = resultsState,
            )
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = SearchUiState(
                filters = searchFiltersState.value.filters.toUiState(),
            ),
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
            OnBackPress -> {
                if (!unselectLastSelectedSearchFilter()) {
                    sendEffect(NavigateBack)
                }
            }

            OnBackIconClick -> searchQuery.value = ""
            OnSearchSubScreenBackPress -> {
                if (!unselectLastSelectedSearchFilter()) {
                    searchQuery.value = ""
                }
            }

            is OnSearchResultItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
            is OnSearchHistoryItemClick -> searchQuery.value = viewEvent.searchQuery
            OnSearchHistoryClear -> searchHistoryManager.clear(viewModelScope)
            is OnSearchQueryChange -> searchQuery.value = viewEvent.updatedQuery
            is OnFilterToggled -> toggleSearchFilter(viewEvent.filter)
            OnSearchQueryClear -> searchQuery.value = ""
        }
    }

    private fun toggleSearchFilter(filter: SearchFilterUiState) {
        searchFiltersState.update { state ->
            val isSelected = state.filters
                .entries
                .firstOrNull { (searchFilter, _) -> searchFilter.label == filter.label }
                ?.value
                ?: return@update state

            state.copy(
                filters = state.filters.mapValues { (searchFilter, selected) ->
                    if (searchFilter.label == filter.label) !selected else selected
                },
                selectedFilterLabels = if (isSelected) {
                    state.selectedFilterLabels - filter.label
                } else {
                    state.selectedFilterLabels
                        .filterNot { it == filter.label }
                        .plus(filter.label)
                },
            )
        }
    }

    private fun unselectLastSelectedSearchFilter(): Boolean {
        val lastLabel = searchFiltersState.value.selectedFilterLabels.lastOrNull() ?: return false
        searchFiltersState.update { state ->
            state.copy(
                filters = state.filters.mapValues { (filter, selected) ->
                    if (filter.label == lastLabel) false else selected
                },
                selectedFilterLabels = state.selectedFilterLabels.dropLast(1),
            )
        }
        return true
    }

    private data class SearchFiltersState(
        val filters: Map<SearchFilter, Boolean>,
        val selectedFilterLabels: List<Int>,
    )

    private fun sendEffect(effect: SearchEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }
}

private fun Map<SearchFilter, Boolean>.toUiState(): ImmutableList<SearchFilterUiState> {
    return map { (filter, selected) ->
        SearchFilterUiState(filter.label, selected)
    }.toImmutableList()
}
