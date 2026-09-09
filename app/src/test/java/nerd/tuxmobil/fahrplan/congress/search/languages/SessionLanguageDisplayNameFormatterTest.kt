package nerd.tuxmobil.fahrplan.congress.search.languages

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SessionLanguageDisplayNameFormatterTest {

    @Test
    fun `format maps known language code to display name`() {
        val name = createFormatter().format("en")
        assertThat(name).isEqualTo("English")
    }

    @Test
    fun `format maps combined language value to joined display names`() {
        val name = createFormatter().format("de, en")
        assertThat(name).isEqualTo("German, English")
    }

    @Test
    fun `format removes formal suffix`() {
        val name = createFormatter().format("de-formal")
        assertThat(name).isEqualTo("German")
    }

    @Test
    fun `format maps Other sentinel to Other display name`() {
        val name = createFormatter().format(SYNTHETIC_LANGUAGE_KEY_OTHER)
        assertThat(name).isEqualTo("Other")
    }

    @Test
    fun `format falls back to raw code for unknown language`() {
        val name = createFormatter().format("xx")
        assertThat(name).isEqualTo("xx")
    }

    private fun createFormatter() = SessionLanguageDisplayNameFormatter(
        FakeLanguageResourceResolver
    )

}
