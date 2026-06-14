package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.NotRecordedSearchFilter
import org.junit.jupiter.api.Test

class SearchFiltersStateTest {

    @Test
    fun `all search filters are initially unselected`() {
        val favoriteFilter = IsFavoriteSearchFilter()
        val alarmFilter = HasAlarmSearchFilter()

        val state = SearchFiltersState.of(
            searchFilters = listOf(favoriteFilter, alarmFilter),
        )

        assertThat(state.uiState).containsExactly(
            SearchFilterUiState(label = favoriteFilter.label, selected = false),
            SearchFilterUiState(label = alarmFilter.label, selected = false),
        ).inOrder()
        assertThat(state.activeFilters).isEmpty()
        assertThat(state.hasSelectedFilters).isFalse()
    }

    @Test
    fun `toggle selects and deselects search filter`() {
        val filter = IsFavoriteSearchFilter()
        val initialState = SearchFiltersState.of(searchFilters = listOf(filter))

        val selectedState = initialState.toggle(
            SearchFilterUiState(label = filter.label, selected = false)
        )

        assertThat(selectedState.uiState).containsExactly(
            SearchFilterUiState(label = filter.label, selected = true)
        )
        assertThat(selectedState.activeFilters).containsExactly(filter)
        assertThat(selectedState.hasSelectedFilters).isTrue()

        val deselectedState = selectedState.toggle(
            SearchFilterUiState(label = filter.label, selected = true)
        )

        assertThat(deselectedState.uiState).containsExactly(
            SearchFilterUiState(label = filter.label, selected = false)
        )
        assertThat(deselectedState.activeFilters).isEmpty()
        assertThat(deselectedState.hasSelectedFilters).isFalse()
    }

    @Test
    fun `unselectLastSelected unselects selected filters in reverse selection order`() {
        val favoriteFilter = IsFavoriteSearchFilter()
        val alarmFilter = HasAlarmSearchFilter()
        val notRecordedFilter = NotRecordedSearchFilter()
        val selectedState = SearchFiltersState
            .of(searchFilters = listOf(favoriteFilter, alarmFilter, notRecordedFilter))
            .toggle(SearchFilterUiState(label = favoriteFilter.label, selected = false))
            .toggle(SearchFilterUiState(label = alarmFilter.label, selected = false))
            .toggle(SearchFilterUiState(label = notRecordedFilter.label, selected = false))

        val notRecordedUnselectedState = selectedState.unselectLastSelected()
        assertThat(notRecordedUnselectedState.uiState).containsExactly(
            SearchFilterUiState(label = favoriteFilter.label, selected = true),
            SearchFilterUiState(label = alarmFilter.label, selected = true),
            SearchFilterUiState(label = notRecordedFilter.label, selected = false),
        ).inOrder()

        val alarmUnselectedState = notRecordedUnselectedState.unselectLastSelected()
        assertThat(alarmUnselectedState.uiState).containsExactly(
            SearchFilterUiState(label = favoriteFilter.label, selected = true),
            SearchFilterUiState(label = alarmFilter.label, selected = false),
            SearchFilterUiState(label = notRecordedFilter.label, selected = false),
        ).inOrder()

        val favoriteUnselectedState = alarmUnselectedState.unselectLastSelected()
        assertThat(favoriteUnselectedState.uiState).containsExactly(
            SearchFilterUiState(label = favoriteFilter.label, selected = false),
            SearchFilterUiState(label = alarmFilter.label, selected = false),
            SearchFilterUiState(label = notRecordedFilter.label, selected = false),
        ).inOrder()
        assertThat(favoriteUnselectedState.activeFilters).isEmpty()
        assertThat(favoriteUnselectedState.hasSelectedFilters).isFalse()
    }

    @Test
    fun `unselectLastSelected keeps state unchanged when no filter is selected`() {
        val filter = IsFavoriteSearchFilter()
        val state = SearchFiltersState.of(searchFilters = listOf(filter))

        assertThat(state.unselectLastSelected()).isEqualTo(state)
    }
}
