package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MarkdownConverterTest {

    @Test
    fun `markdownLinksToHtmlLinks returns a single Markdown link as HTML`() {
        val markdown = "[Chaos Computer Club](https://www.ccc.de)"
        val htmlLink = """<a href="https://www.ccc.de">Chaos Computer Club</a>"""
        assertThat(MarkdownConverter.markdownLinksToHtmlLinks(markdown)).isEqualTo(htmlLink)
    }

    @Test
    fun `markdownLinksToHtmlLinks returns multiple Markdown links as HTML`() {
        val markdown = "[Chaos Computer Club](https://www.ccc.de)<br>" +
                "[Bundestag](https://www.bundestag.de)"
        val htmlLink = """<a href="https://www.ccc.de">Chaos Computer Club</a><br>""" +
                """<a href="https://www.bundestag.de">Bundestag</a>"""
        assertThat(MarkdownConverter.markdownLinksToHtmlLinks(markdown)).isEqualTo(htmlLink)
    }

}
