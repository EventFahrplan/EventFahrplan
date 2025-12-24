package nerd.tuxmobil.fahrplan.congress.search

sealed interface SearchViewEvent {
    data object OnBackPress : SearchViewEvent
    data object OnBackIconClick : SearchViewEvent
    data object OnSearchSubScreenBackPress : SearchViewEvent
    data object OnSearchQueryClear : SearchViewEvent
    data class OnSearchHistoryItemClick(val searchQuery: String) : SearchViewEvent
    data object OnSearchHistoryClear : SearchViewEvent
    data class OnSearchQueryChange(val updatedQuery: String) : SearchViewEvent
    data class OnFilterToggled(val filter: SearchFilterUiState) : SearchViewEvent
    data class OnSearchResultItemClick(val sessionId: String) : SearchViewEvent
}
