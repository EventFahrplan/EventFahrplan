package nerd.tuxmobil.fahrplan.congress.search

sealed interface SearchResultParameter {

    data class SearchResult(
        val id: String,
        val title: SearchResultProperty<String>,
        val speakerNames: SearchResultProperty<String>,
        val startsAt: SearchResultProperty<String>,
    ) : SearchResultParameter

}
