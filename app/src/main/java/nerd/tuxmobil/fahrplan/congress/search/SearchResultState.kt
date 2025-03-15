package nerd.tuxmobil.fahrplan.congress.search

sealed interface SearchResultState {
    data object Loading : SearchResultState
    data class Success(val parameters: List<SearchResultParameter>) : SearchResultState
}
