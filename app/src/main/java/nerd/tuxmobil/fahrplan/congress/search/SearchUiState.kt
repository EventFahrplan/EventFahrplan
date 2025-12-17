package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading

data class SearchUiState(
    val query: String = "",
    val filters: ImmutableList<SearchFilterUiState> = persistentListOf(),
    val resultsState: SearchResultState = Loading,
)
