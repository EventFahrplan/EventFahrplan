package info.metadude.android.eventfahrplan.commons.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StringSanitizationTest {

    @Nested
    inner class Sanitize {

        @Test
        fun `removes leading and trailing whitespace`() {
            assertThat("  foobar  ".sanitize()).isEqualTo("foobar")
        }

        @Test
        fun `removes zero width no break space`() {
            val s = "$ZERO_WIDTH_NO_BREAK_SPACE  foobar${ZERO_WIDTH_NO_BREAK_SPACE}${ZERO_WIDTH_NO_BREAK_SPACE}"
            assertThat(s.sanitize()).isEqualTo("foobar")
        }

        @Test
        fun `normalizes line breaks`() {
            assertThat("\r\nfoobar\r\n\r\n".sanitize()).isEqualTo("foobar")
        }

        @Test
        fun `applies all normalizations together`() {
            val s = "$ZERO_WIDTH_NO_BREAK_SPACE  \r\n foobar\r\n${ZERO_WIDTH_NO_BREAK_SPACE}\r\n  "
            assertThat(s.sanitize()).isEqualTo("foobar")
        }

        @Test
        fun `keeps asterisk character untouched`() {
            val s = "Workshop für Entwickler*innen"
            assertThat(s.sanitize()).isEqualTo(s)
        }

        @Test
        fun `blank input becomes empty`() {
            assertThat("   ".sanitize()).isEmpty()
        }

        @Test
        fun `null input becomes empty`() {
            assertThat(null.sanitize()).isEmpty()
        }
    }

    @Nested
    inner class SanitizeMarkdown {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(
            delimiter = '|',
            textBlock = """
            Entwickler*innen                 | Entwickler\*innen
            Nutzer*in                        | Nutzer\*in
            Künstler*Innen                   | Künstler\*Innen
            Ärzt*innen                       | Ärzt\*innen
            Lehrer*in und Schüler*in         | Lehrer\*in und Schüler\*in
            Team2*3                          | Team2\*3"""
        )
        fun `escapes asterisk within a word to prevent unintended italic formatting`(input: String, expected: String) {
            assertThat(input.sanitizeMarkdown()).isEqualTo(expected)
        }

        @Test
        fun `keeps already escaped asterisk untouched`() {
            val s = """Entwickler\*innen"""
            assertThat(s.sanitizeMarkdown()).isEqualTo(s)
        }

        @Test
        fun `keeps intended emphasis untouched`() {
            val s = "Some *italic* and **bold** text, **bold**suffix and 2 * 3"
            assertThat(s.sanitizeMarkdown()).isEqualTo(s)
        }

        @Test
        fun `keeps empty input empty`() {
            assertThat("".sanitizeMarkdown()).isEmpty()
        }
    }

}
