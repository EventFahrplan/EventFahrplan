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
    RecordedSearchFilter(),
    NotRecordedSearchFilter(),
    WithinSpeakerNamesSearchFilter(),
    WithinTitleSubtitleSearchFilter(),
    WithinTrackNameSearchFilter(),
)

internal data class SearchFiltersState(
    private val selectedByFilter: Map<SearchFilter, Boolean>,
) {

    companion object {
        fun of(searchFilters: List<SearchFilter>) = SearchFiltersState(
            selectedByFilter = searchFilters.associateWith { false },
        )
    }

    val activeFilters: Set<SearchFilter>
        get() = selectedByFilter
            .asSequence()
            .filter { it.value }
            .map { it.key }
            .toSet()

    val uiState: ImmutableList<SearchFilterUiState>
        get() = selectedByFilter.map { (filter, selected) ->
            SearchFilterUiState(filter.label, selected)
        }.toImmutableList()

    fun toggle(filter: SearchFilterUiState): SearchFiltersState {
        selectedByFilter
            .entries
            .firstOrNull { (searchFilter, _) -> searchFilter.label == filter.label }
            ?: return this

        return copy(
            selectedByFilter = selectedByFilter.mapValues { (searchFilter, selected) ->
                if (searchFilter.label == filter.label) !selected else selected
            },
        )
    }

    fun unselect(label: Int): SearchFiltersState {
        return copy(
            selectedByFilter = selectedByFilter.mapValues { (filter, selected) ->
                if (filter.label == label) false else selected
            },
        )
    }

}
