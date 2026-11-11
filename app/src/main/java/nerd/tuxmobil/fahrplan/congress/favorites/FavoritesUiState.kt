package nerd.tuxmobil.fahrplan.congress.favorites

import nerd.tuxmobil.fahrplan.congress.search.SearchResultParameter

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(val parameters: List<SearchResultParameter>) : FavoritesUiState
}
