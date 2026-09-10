package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.search.filters.HasAlarmSearchFilter
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import org.junit.jupiter.api.Test

class SearchFiltersStateTest {
    @Test
    fun `filters are initially unselected`() {
        val favorite = IsFavoriteSearchFilter()
        val alarm = HasAlarmSearchFilter()
        val state = SearchFiltersState.of(listOf(favorite, alarm))

        assertThat(state.uiState)
            .containsExactly(
                SearchFilterUiState(favorite.label, false),
                SearchFilterUiState(alarm.label, false)
            )
            .inOrder()
        assertThat(state.activeFilters).isEmpty()
    }

    @Test
    fun `toggle selects and deselects filter`() {
        val filter = IsFavoriteSearchFilter()
        val state = SearchFiltersState.of(listOf(filter))
        val selected = state.toggle(SearchFilterUiState(filter.label, false))

        assertThat(selected.activeFilters).containsExactly(filter)
        assertThat(selected.toggle(SearchFilterUiState(filter.label, true)).activeFilters).isEmpty()
    }

    @Test
    fun `unselect clears requested filter and unknown labels are unchanged`() {
        val favorite = IsFavoriteSearchFilter()
        val alarm = HasAlarmSearchFilter()
        val selected = SearchFiltersState.of(listOf(favorite, alarm))
            .toggle(SearchFilterUiState(favorite.label, false))
            .toggle(SearchFilterUiState(alarm.label, false))
        val unselected = selected.unselect(alarm.label)

        assertThat(unselected.uiState)
            .containsExactly(
                SearchFilterUiState(favorite.label, true),
                SearchFilterUiState(alarm.label, false)
            )
            .inOrder()
        assertThat(unselected.unselect(999)).isEqualTo(unselected)
    }

}
