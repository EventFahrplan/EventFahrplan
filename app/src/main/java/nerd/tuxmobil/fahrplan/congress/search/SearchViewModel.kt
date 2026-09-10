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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
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
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnLanguageFilterToggled
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchHistoryItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryChange
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchQueryClear
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchResultItemClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchSubScreenBackPress
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFilterUiState
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFiltersState
import nerd.tuxmobil.fahrplan.congress.search.languages.getDistinctLanguageKeys
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: AppRepository,
    private val searchQueryFilter: SearchQueryFilter,
    private val searchHistoryManager: SearchHistoryManager,
    private val searchResultParameterFactory: SearchResultParameterFactory,
    private val languageFiltersState: SearchLanguageFiltersState,
    private val languageFilterUiStateFactory: SearchLanguageFilterUiState.Factory,
    searchFilters: List<SearchFilter> = SUPPORTED_SEARCH_FILTERS,
) : ViewModel() {

    private companion object {
        val FINISH_TYPING_SEARCH_QUERY_DELAY = 1_000.milliseconds
    }

    private val mutableEffects = Channel<SearchEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    private val searchFiltersState = MutableStateFlow(
        SearchFiltersState.of(searchFilters)
    )

    private data class LanguageFilterState(
        val selectedLanguageKeys: Set<String>,
        val uiState: SearchLanguageFilterUiState?,
    )

    private sealed interface FilterSelection {
        data class Chip(val label: Int) : FilterSelection
        data class Language(val key: String) : FilterSelection
    }

    private val selectionHistory = mutableListOf<FilterSelection>()

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    private val sessions = repository.sessions.shareIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        replay = 1,
    )

    private val languageFilterState = combine(
        sessions
            .map { it.getDistinctLanguageKeys() }
            .distinctUntilChanged(),
        languageFiltersState.selectedLanguageKeys,
    ) { languageKeys, selectedLanguageKeys ->
        val languageKeySet = languageKeys.toSet()
        selectionHistory.removeAll { selection ->
            selection is FilterSelection.Language && selection.key !in languageKeySet
        }
        val selectedLanguageSet = selectedLanguageKeys.intersect(languageKeySet)
        if (selectedLanguageSet.size != selectedLanguageKeys.size) {
            languageFiltersState.retainKeys(languageKeySet)
        }
        LanguageFilterState(
            selectedLanguageKeys = selectedLanguageSet,
            uiState = languageFilterUiStateFactory.of(languageKeys, selectedLanguageSet),
        )
    }

    val uiState: StateFlow<SearchUiState> =
        combine(
            searchQuery,
            searchFiltersState,
            sessions,
            searchHistoryManager.searchHistory,
            languageFilterState,
        ) { query, searchFiltersState, sessions, searchHistory, languageFilterState ->
            val activeFilters = languageFiltersState.augmentActiveFilters(
                filters = searchFiltersState.activeFilters,
                selectedLanguages = languageFilterState.selectedLanguageKeys,
            )
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
                filters = searchFiltersState.uiState,
                languageFilter = languageFilterState.uiState,
                resultsState = resultsState,
            )
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = SearchUiState(
                filters = searchFiltersState.value.uiState,
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
            is OnFilterToggled -> handleFilterToggled(viewEvent.state)
            is OnLanguageFilterToggled -> handleLanguageFilterToggled(viewEvent.filterKey)
            OnSearchQueryClear -> searchQuery.value = ""
        }
    }

    private fun handleFilterToggled(searchFilterUiState: SearchFilterUiState) {
        val selecting = !searchFilterUiState.selected
        searchFiltersState.update { it.toggle(searchFilterUiState) }
        val selection = FilterSelection.Chip(searchFilterUiState.label)
        selectionHistory.removeAll { it == selection }
        if (selecting) selectionHistory.add(selection)
    }

    private fun handleLanguageFilterToggled(filterKey: String) {
        val selecting = filterKey !in languageFiltersState.selectedLanguageKeys.value
        languageFiltersState.toggle(filterKey)
        val selection = FilterSelection.Language(filterKey)
        selectionHistory.removeAll { it == selection }
        if (selecting) selectionHistory.add(selection)
    }

    private fun unselectLastSelectedSearchFilter(): Boolean {
        while (selectionHistory.isNotEmpty()) {
            when (val selection = selectionHistory.removeAt(selectionHistory.lastIndex)) {
                is FilterSelection.Chip -> {
                    if (searchFiltersState.value.activeFilters.any { it.label == selection.label }) {
                        searchFiltersState.update { it.unselect(selection.label) }
                        return true
                    }
                }

                is FilterSelection.Language -> if (languageFiltersState.unselect(selection.key)) return true
            }
        }
        return false
    }

    private fun sendEffect(effect: SearchEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }
}
