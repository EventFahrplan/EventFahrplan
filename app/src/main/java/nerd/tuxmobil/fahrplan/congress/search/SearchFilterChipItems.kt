package nerd.tuxmobil.fahrplan.congress.search

import nerd.tuxmobil.fahrplan.congress.R

internal sealed interface SearchFilterChipItem {
    data class Filter(val state: SearchFilterUiState) : SearchFilterChipItem
    data object LanguageFilter : SearchFilterChipItem
}

internal fun searchFilterChipItems(
    filters: List<SearchFilterUiState>,
    hasLanguageFilter: Boolean,
    anchorLabel: Int = R.string.search_filter_not_recorded,
): List<SearchFilterChipItem> {
    val items: MutableList<SearchFilterChipItem> = filters
        .map { SearchFilterChipItem.Filter(it) }
        .toMutableList()
    if (hasLanguageFilter) {
        val anchorIndex = filters.indexOfFirst { it.label == anchorLabel }
        items.add(
            if (anchorIndex >= 0) anchorIndex + 1 else items.size,
            SearchFilterChipItem.LanguageFilter,
        )
    }
    return items
}
