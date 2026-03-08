package nerd.tuxmobil.fahrplan.congress.favorites

import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter

sealed interface StarredListUiState {
    data object Loading : StarredListUiState
    data class Success(val parameters: List<SearchResultParameter>) : StarredListUiState
}
