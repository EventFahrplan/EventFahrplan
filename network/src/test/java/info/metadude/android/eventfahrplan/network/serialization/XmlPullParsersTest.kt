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

    @Nested
    inner class GetSanitizedAttributeNullableValue {

        @Test
        fun `getSanitizedAttributeNullableValue returns null if attribute is not present`() {
            val parser = createParser(
                text = "  foobar  ",
                attributeName = "",
            )
            assertThat(parser.getSanitizedAttributeNullableValue("")).isEqualTo(null)
        }

        @Test
        fun `getSanitizedAttributeNullableValue returns null if text is empty`() {
            val parser = createParser(
                text = "   ",
                attributeName = "name",
            )
            assertThat(parser.getSanitizedAttributeNullableValue("name")).isEqualTo(null)
        }

        @Test
        fun `getSanitizedAttributeNullableValue text without leading and trailing whitespace`() {
            val parser = createParser(
                text = "  foobar  ",
                attributeName = "name",
            )
            assertThat(parser.getSanitizedAttributeNullableValue("name")).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedAttributeNullableValue text without zero with no break space`() {
            val parser = createParser(
                text = "$ZERO_WIDTH_NO_BREAK_SPACE  foobar$ZERO_WIDTH_NO_BREAK_SPACE$ZERO_WIDTH_NO_BREAK_SPACE",
                attributeName = "name",
            )
            assertThat(parser.getSanitizedAttributeNullableValue("name")).isEqualTo("foobar")
        }

        @Test
        fun `getSanitizedAttributeNullableValue text without with cleaned up line breaks`() {
            val parser = createParser(
                text = "\r\nfoobar\r\n\r\n",
                attributeName = "name",
            )
            assertThat(parser.getSanitizedAttributeNullableValue("name")).isEqualTo("foobar")
        }

    }

    private fun createParser(text: String, attributeName: String = "") = mock<XmlPullParser> {
        val textValue = if (attributeName.isEmpty()) null else text
        on { this.getAttributeValue(null, attributeName) } doReturn textValue
        on { this.text } doReturn text
    }

}
