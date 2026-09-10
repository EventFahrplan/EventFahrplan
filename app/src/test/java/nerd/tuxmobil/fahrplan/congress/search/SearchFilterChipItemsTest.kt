package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import org.junit.jupiter.api.Test

class SearchFilterChipItemsTest {

    private val filters = listOf(
        SearchFilterUiState(1, false),
        SearchFilterUiState(R.string.search_filter_not_recorded, false),
        SearchFilterUiState(2, true),
    )

    @Test
    fun `language item follows anchor and preserves filter order`() {
        val items = searchFilterChipItems(filters, hasLanguageFilter = true)
        assertThat(items).containsExactly(
            SearchFilterChipItem.Filter(filters[0]),
            SearchFilterChipItem.Filter(filters[1]),
            SearchFilterChipItem.LanguageFilter,
            SearchFilterChipItem.Filter(filters[2]),
        ).inOrder()
    }

    @Test
    fun `language item is appended when anchor is absent`() {
        val withoutAnchor = listOf(filters[0], filters[2])
        val items = searchFilterChipItems(withoutAnchor, hasLanguageFilter = true)
        assertThat(items.last()).isEqualTo(SearchFilterChipItem.LanguageFilter)
    }

    @Test
    fun `language item is omitted when unavailable`() {
        val items = searchFilterChipItems(filters, hasLanguageFilter = false)
        val expected = filters.map { SearchFilterChipItem.Filter(it) }
        assertThat(items).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `language item is the only item for empty filters`() {
        val items = searchFilterChipItems(emptyList(), hasLanguageFilter = true)
        assertThat(items).containsExactly(SearchFilterChipItem.LanguageFilter)
    }

}
