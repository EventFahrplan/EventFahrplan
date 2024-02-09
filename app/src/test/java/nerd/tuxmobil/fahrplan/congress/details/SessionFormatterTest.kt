package nerd.tuxmobil.fahrplan.congress.details

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SessionFormatterTest {

    private val formatter = SessionFormatter()

    @Test
    fun `getFormattedLinks returns an empty string`() {
        val links = ""
        val expected = ""
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedLinks returns the given unmodified string`() {
        val links = "[VOC projects](https://www.voc.com/projects/)"
        val expected = "[VOC projects](https://www.voc.com/projects/)"
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedLinks returns a br separated string`() {
        val links = "[VOC projects](https://www.voc.com/projects/),[POC](https://poc.com/QXut1XBymAk)"
        val expected = "[VOC projects](https://www.voc.com/projects/)<br>[POC](https://poc.com/QXut1XBymAk)"
        assertThat(formatter.getFormattedLinks(links)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedUrl returns an empty string`() {
        val url = ""
        val expected = ""
        assertThat(formatter.getFormattedUrl(url)).isEqualTo(expected)
    }

    @Test
    fun `getFormattedUrl returns an HTML formatted weblink`() {
        val url = "https://example.com/talk.html"
        val expected = """<a href="https://example.com/talk.html">https://example.com/talk.html</a>"""
        assertThat(formatter.getFormattedUrl(url)).isEqualTo(expected)
    }

}
