package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading

data class SearchUiState(
    val query: String = "",
    val resultsState: SearchResultState = Loading,
)
