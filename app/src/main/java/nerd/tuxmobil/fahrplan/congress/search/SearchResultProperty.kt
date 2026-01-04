package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE

data class SearchResultProperty<T>(
    val value: T,
    val contentDescription: String,
    val tenseType: TenseType = FUTURE,
)
