package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.collections.immutable.ImmutableList

sealed interface SearchResultState {
    data object Loading : SearchResultState
    data class NoSearchResults(val backEvent: SearchViewEvent) : SearchResultState
    data class SearchHistory(val searchTerms: ImmutableList<String>) : SearchResultState
    data class SearchResults(val searchResults: ImmutableList<SearchResultParameter>) : SearchResultState
}
