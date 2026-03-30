package info.metadude.android.eventfahrplan.commons.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.MARKDOWN_LINKS_DELIMITER
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SessionLinksTest {

    @Nested
    inner class MarkdownLinkSegment {

        @Test
        fun `uses urlName when href null`() {
            assertThat(markdownLinkSegment("Label", null)).isEqualTo("[Label](http://Label)")
        }

        @Test
        fun `keeps scheme`() {
            assertThat(markdownLinkSegment("Docs", "https://x.y/z")).isEqualTo("[Docs](https://x.y/z)")
        }

        @Test
        fun `adds http scheme is lacking`() {
            assertThat(markdownLinkSegment("x", "example.com")).isEqualTo("[x](http://example.com)")
        }

    }

    @Nested
    inner class AppendMarkdownLink {

        @Test
        fun `joins with delimiter`() {
            val first = markdownLinkSegment("A", "http://a")
            val second = appendMarkdownLink(first, "B", "http://b")
            assertThat(second).isEqualTo("[A](http://a)$MARKDOWN_LINKS_DELIMITER[B](http://b)")
        }

        @Test
        fun `appends to empty`() {
            assertThat(appendMarkdownLink("", "T", null)).isEqualTo("[T](http://T)")
        }

    }

}
