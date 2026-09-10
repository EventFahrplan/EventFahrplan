package nerd.tuxmobil.fahrplan.congress.search.languages

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class LanguagesSearchFilterTest {

    @Test
    fun `isMatch returns true when session language is selected`() {
        val matches = createFilter(setOf("en"))
            .isMatch(Session(sessionId = "1", language = "en"))
        assertThat(matches).isTrue()
    }

    @Test
    fun `isMatch returns false when session language is not selected`() {
        val matches = createFilter(setOf("en"))
            .isMatch(Session(sessionId = "1", language = "de"))
        assertThat(matches).isFalse()
    }

    @Test
    fun `isMatch matches combined language value as raw database value`() {
        val filter = createFilter(setOf("de, en"))
        assertThat(filter.isMatch(Session(sessionId = "1", language = "de, en"))).isTrue()
        assertThat(filter.isMatch(Session(sessionId = "2", language = "de"))).isFalse()
    }

    @Test
    fun `isMatch matches a combined language value with a different order`() {
        val filter = createFilter(setOf("de, en"))
        assertThat(filter.isMatch(Session(sessionId = "1", language = "en, de"))).isTrue()
    }

    @Test
    fun `isMatch matches empty language when Other is selected`() {
        val filter = createFilter(setOf(SYNTHETIC_LANGUAGE_KEY_OTHER))
        assertThat(filter.isMatch(Session(sessionId = "1", language = ""))).isTrue()
        assertThat(filter.isMatch(Session(sessionId = "2", language = "en"))).isFalse()
    }

    @Test
    fun `isMatch returns false when no languages are selected`() {
        val matches = createFilter(emptySet())
            .isMatch(Session(sessionId = "1", language = "en"))
        assertThat(matches).isFalse()
    }

    private fun createFilter(languages: Set<String>) = LanguagesSearchFilter(languages)

    private fun LanguagesSearchFilter.isMatch(session: Session) = isMatch(session, query = "")

}
