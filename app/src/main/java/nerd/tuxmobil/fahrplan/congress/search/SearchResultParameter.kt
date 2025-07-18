package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.commons.DaySeparatorProperty

sealed interface SearchResultParameter {

    data class Separator(
        val daySeparator: DaySeparatorProperty<String>,
    ) : SearchResultParameter

    data class SearchResult(
        val id: String,
        val title: SearchResultProperty<String>,
        val speakerNames: SearchResultProperty<String>,
        val startsAt: SearchResultProperty<String>,
    ) : SearchResultParameter

}
