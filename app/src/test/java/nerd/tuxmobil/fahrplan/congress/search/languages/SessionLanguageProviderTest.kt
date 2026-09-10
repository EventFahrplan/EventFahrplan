package nerd.tuxmobil.fahrplan.congress.search.languages

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class SessionLanguageProviderTest {

    @Test
    fun `getDistinctLanguageKeys returns empty list when sessions are empty`() {
        val keys = emptyList<Session>().getDistinctLanguageKeys()
        assertThat(keys).isEmpty()
    }

    @Test
    fun `getDistinctLanguageKeys returns sorted distinct non-empty language values`() {
        val sessions = listOf(
            Session(sessionId = "1", language = "de"),
            Session(sessionId = "2", language = "en"),
            Session(sessionId = "3", language = "de"),
        )
        val keys = sessions.getDistinctLanguageKeys()
        assertThat(keys).containsExactly("de", "en").inOrder()
    }

    @Test
    fun `getDistinctLanguageKeys normalizes comma-separated language values before removing duplicates`() {
        val sessions = listOf(
            Session(sessionId = "1", language = "de, en"),
            Session(sessionId = "2", language = "en, de"),
        )

        val keys = sessions.getDistinctLanguageKeys()

        assertThat(keys).containsExactly("de, en")
    }

    @Test
    fun `getDistinctLanguageKeys appends OTHER when empty language exists`() {
        val sessions = listOf(
            Session(sessionId = "1", language = "en"),
            Session(sessionId = "2", language = ""),
        )
        val keys = sessions.getDistinctLanguageKeys()
        assertThat(keys).containsExactly("en", SYNTHETIC_LANGUAGE_KEY_OTHER).inOrder()
    }

}
