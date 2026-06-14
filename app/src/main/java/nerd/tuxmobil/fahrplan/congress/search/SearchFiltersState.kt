package nerd.tuxmobil.fahrplan.congress.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotRecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.RecordedSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinSpeakerNamesSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTitleSubtitleSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.WithinTrackNameSearchFilter

internal val SUPPORTED_SEARCH_FILTERS = listOf(
    IsFavoriteSearchFilter(),
    NotFavoriteSearchFilter(),
    HasAlarmSearchFilter(),
    NotRecordedSearchFilter(),
    RecordedSearchFilter(),
    WithinSpeakerNamesSearchFilter(),
    WithinTitleSubtitleSearchFilter(),
    WithinTrackNameSearchFilter(),
)

internal data class SearchFiltersState(
    private val filters: Map<SearchFilter, Boolean>,
    private val selectedFilterLabels: List<Int>,
) {

    companion object {
        fun of(searchFilters: List<SearchFilter>) = SearchFiltersState(
            filters = searchFilters.associateWith { false },
            selectedFilterLabels = emptyList(),
        )
    }

    val activeFilters: Set<SearchFilter>
        get() = filters
            .asSequence()
            .filter { it.value }
            .map { it.key }
            .toSet()

    val uiState: ImmutableList<SearchFilterUiState>
        get() = filters.map { (filter, selected) ->
            SearchFilterUiState(filter.label, selected)
        }.toImmutableList()

    val hasSelectedFilters: Boolean
        get() = selectedFilterLabels.isNotEmpty()

    fun toggle(filter: SearchFilterUiState): SearchFiltersState {
        val isSelected = filters
            .entries
            .firstOrNull { (searchFilter, _) -> searchFilter.label == filter.label }
            ?.value
            ?: return this

        return copy(
            filters = filters.mapValues { (searchFilter, selected) ->
                if (searchFilter.label == filter.label) !selected else selected
            },
            selectedFilterLabels = if (isSelected) {
                selectedFilterLabels - filter.label
            } else {
                selectedFilterLabels
                    .filterNot { it == filter.label }
                    .plus(filter.label)
            },
        )
    }

    fun unselectLastSelected(): SearchFiltersState {
        val lastLabel = selectedFilterLabels.lastOrNull() ?: return this
        return copy(
            filters = filters.mapValues { (filter, selected) ->
                if (filter.label == lastLabel) false else selected
            },
            selectedFilterLabels = selectedFilterLabels.dropLast(1),
        )
    }

}
