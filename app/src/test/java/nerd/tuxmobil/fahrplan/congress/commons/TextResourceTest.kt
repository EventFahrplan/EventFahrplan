package nerd.tuxmobil.fahrplan.congress.commons

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.commons.TextResource.Html
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TextResourceTest {

    @Test
    fun `of returns HTML formatted link with title`() {
        assertThat(Html.of(url = "https://example.com", text = "Example website"))
            .isEqualTo(Html("""<a href="https://example.com">Example website</a>"""))
    }

    @Test
    fun `of returns HTML formatted link`() {
        assertThat(Html.of(url = "https://example.com", text = "https://example.com"))
            .isEqualTo(Html("""<a href="https://example.com">https://example.com</a>"""))
    }

    @Test
    fun `of returns plain text`() {
        assertThat(Html.of(url = "Visit example.com", text = null))
            .isEqualTo(Html("Visit example.com"))
    }

    @Test
    fun `of throws exception if url is empty`() {
        assertThrows<IllegalArgumentException> {
            Html.of(url = "", text = null)
        }
    }

    @Test
    fun `of throws exception if url and text are passed as plain text`() {
        assertThrows<IllegalArgumentException> {
            Html.of(url = "Visit example.com", text = "Visit example.com")
        }
    }

}
