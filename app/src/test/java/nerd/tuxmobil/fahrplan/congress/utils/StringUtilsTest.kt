package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `getHtmlLinkFromMarkdown returns a single Markdown link as HTML`() {
        val markdown = "[Chaos Computer Club](https://www.ccc.de)"
        val htmlLink = """<a href="https://www.ccc.de">Chaos Computer Club</a>"""
        assertThat(StringUtils.getHtmlLinkFromMarkdown(markdown)).isEqualTo(htmlLink)
    }

    @Test
    fun `getHtmlLinkFromMarkdown returns multiple Markdown links as HTML`() {
        val markdown = "[Chaos Computer Club](https://www.ccc.de)<br>" +
                "[Bundestag](https://www.bundestag.de)"
        val htmlLink = """<a href="https://www.ccc.de">Chaos Computer Club</a><br>""" +
                """<a href="https://www.bundestag.de">Bundestag</a>"""
        assertThat(StringUtils.getHtmlLinkFromMarkdown(markdown)).isEqualTo(htmlLink)
    }

}
