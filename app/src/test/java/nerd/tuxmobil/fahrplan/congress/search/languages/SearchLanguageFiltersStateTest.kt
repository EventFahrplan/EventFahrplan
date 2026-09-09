package nerd.tuxmobil.fahrplan.congress.search.languages

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.search.filters.IsFavoriteSearchFilter
import org.junit.jupiter.api.Test

class SearchLanguageFiltersStateTest {
    private val state = SearchLanguageFiltersState()

    @Test
    fun `toggle adds and removes keys`() {
        state.toggle("en")
        state.toggle("de")
        assertThat(state.selectedLanguageKeys.value).containsExactly("en", "de")
        state.toggle("en")
        assertThat(state.selectedLanguageKeys.value).containsExactly("de")
    }

    @Test
    fun `unselect removes requested language`() {
        state.toggle("en")
        state.toggle("de")
        assertThat(state.unselect("de")).isTrue()
        assertThat(state.selectedLanguageKeys.value).containsExactly("en")
        assertThat(state.unselect("de")).isFalse()
    }

    @Test
    fun `augmentActiveFilters adds language filter only when selections exist`() {
        val filters = setOf(IsFavoriteSearchFilter())
        assertThat(state.augmentActiveFilters(filters, emptySet())).isEqualTo(filters)
        assertThat(state.augmentActiveFilters(filters, setOf("en"))).hasSize(2)
    }

}
