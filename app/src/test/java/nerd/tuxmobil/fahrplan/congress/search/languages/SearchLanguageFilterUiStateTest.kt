package nerd.tuxmobil.fahrplan.congress.search.languages

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.search.languages.SearchLanguageFilterUiState.Factory
import org.junit.jupiter.api.Test

class SearchLanguageFilterUiStateTest {

    private val factory = Factory(FakeLanguageResourceResolver)

    @Test
    fun `of maps selection flags and derives selected count`() {
        val state = factory.of(
            languageKeys = listOf("de", "en"),
            selectedLanguages = setOf("en"),
        )

        assertThat(state?.languageOptions?.map { it.filterKey to it.selected })
            .containsExactly("de" to false, "en" to true)
            .inOrder()
        assertThat(state?.selectedCount).isEqualTo(1)
        assertThat(state?.languageOptions?.map { it.displayName })
            .containsExactly("German", "English")
            .inOrder()
    }

    @Test
    fun `of is disabled when only a single language option exists`() {
        val state = factory.of(
            languageKeys = listOf(SYNTHETIC_LANGUAGE_KEY_OTHER),
            selectedLanguages = emptySet(),
        )

        assertThat(state?.enabled).isFalse()
    }

    @Test
    fun `of is enabled when multiple language options exist`() {
        val state = factory.of(
            languageKeys = listOf("de", "en"),
            selectedLanguages = emptySet(),
        )

        assertThat(state?.enabled).isTrue()
    }

}
