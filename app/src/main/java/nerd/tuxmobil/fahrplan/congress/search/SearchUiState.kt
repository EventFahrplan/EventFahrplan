package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nerd.tuxmobil.fahrplan.congress.search.SearchResultState.Loading
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFilterUiState

data class SearchUiState(
    val query: String = "",
    val filters: ImmutableList<SearchFilterUiState> = persistentListOf(),
    val languageFilter: SearchLanguageFilterUiState? = null,
    val resultsState: SearchResultState = Loading,
)
