package nerd.tuxmobil.fahrplan.congress.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.search.Chips.Item
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Success
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackIconClick
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnBackPress
import nerd.tuxmobil.fahrplan.congress.search.SearchViewEvent.OnSearchChipItemClick
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

    var screenNavigation: ScreenNavigation? = null

    private val mutableNavigateBack = Channel<Unit>()
    val navigateBack = mutableNavigateBack.receiveAsFlow()

    var searchQuery by mutableStateOf("")
        private set

    private var mutableChipItems = MutableStateFlow<Set<Item>>(emptySet())
    val chipItems = mutableChipItems.asStateFlow()

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    init {
        mutableChipItems.value = setOf(
            Item(text = "Is favored", selected = false),
            Item(text = "Not recorded", selected = false),
            Item(text = "Has alarm", selected = false),
            Item(text = "Within speaker names", selected = false),
            Item(text = "Day 1", selected = false),
            Item(text = "", selected = false),
        )
    }

    val searchResultsState: StateFlow<SearchResultState> = combine(
        snapshotFlow { searchQuery },
        chipItems,
        repository.sessions
    ) { searchQuery, chipItems, sessions ->
        Success(
            when (searchQuery.isEmpty()) {
                true -> emptyList()
                false -> {
                    var filteredSessions = sessions.filterAll(searchQuery)
                    if (chipItems.contains(Item(text = "Is favored", selected = true))) {
                        filteredSessions = filteredSessions.filterFavored()
                    }
                    if (chipItems.contains(Item(text = "Not recorded", selected = true))) {
                        filteredSessions = filteredSessions.filterNotRecorded()
                    }
                    searchResultParameterFactory.createSearchResults(filteredSessions, useDeviceTimeZone)
                }
            }
        )
    }
    .stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = WhileSubscribed(5_000)
    )
    val searchHistory = searchHistoryManager.searchHistory

    init {
        snapshotFlow { searchQuery }
            .debounce(FINISH_TYPING_SEARCH_QUERY_DELAY)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .onEach { searchHistoryManager.append(viewModelScope, it) }
            .launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: SearchViewEvent) {
        when (viewEvent) {
            OnBackPress -> mutableNavigateBack.sendOneTimeEvent(Unit)
            OnBackIconClick -> searchQuery = ""
            OnSearchSubScreenBackPress -> searchQuery = ""
            is OnSearchResultItemClick -> screenNavigation?.navigateToSessionDetails(viewEvent.sessionId)
            is OnSearchHistoryItemClick -> searchQuery = viewEvent.searchQuery
            OnSearchHistoryClear -> searchHistoryManager.clear(viewModelScope)
            is OnSearchQueryChange -> searchQuery = viewEvent.updatedQuery
            is OnSearchChipItemClick -> updateChipItems(viewEvent.chipItem)
            OnSearchQueryClear -> searchQuery = ""
        }
    }

    private fun updateChipItems(chipItem: Item) {
        val new = mutableChipItems.value.map {
            if (it.text == chipItem.text) it.copy(selected = !it.selected) else it
        }.toSet()
        mutableChipItems.value = new
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }


}
