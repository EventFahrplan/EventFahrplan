package info.metadude.android.eventfahrplan.network.serialization

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.xmlpull.v1.XmlPullParser

class XmlPullParsersTest {

    @Nested
    inner class GetSanitizedText {

        @Test
        fun `getSanitizedText returns text without leading and trailing whitespace`() {
            val parser = createParser(text = "  foobar  ")
            assertThat(parser.getSanitizedText()).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedText returns text without zero with no break space`() {
            val parser = createParser(text = "$ZERO_WIDTH_NO_BREAK_SPACE  foobar$ZERO_WIDTH_NO_BREAK_SPACE$ZERO_WIDTH_NO_BREAK_SPACE")
            assertThat(parser.getSanitizedText()).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedText returns text without with cleaned up line breaks`() {
            val parser = createParser(text = "\r\nfoobar\r\n\r\n")
            assertThat(parser.getSanitizedText()).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedText returns text with all unwanted characters removed`() {
            val parser = createParser(text = "$ZERO_WIDTH_NO_BREAK_SPACE  \r\n foobar\r\n$ZERO_WIDTH_NO_BREAK_SPACE\r\n  ")
            assertThat(parser.getSanitizedText()).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedText returns empty text`() {
            val parser = createParser(text = "   ")
            assertThat(parser.getSanitizedText()).isEmpty()
        }

    }

    private fun createParser(text: String, attributeName: String = "") = mock<XmlPullParser> {
        on { this.text } doReturn text
    }

}
