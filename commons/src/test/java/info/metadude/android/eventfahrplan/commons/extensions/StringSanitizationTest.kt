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
        fun `keeps asterisk and bullet point characters untouched`() {
            val s = "•\tWorkshop für Entwickler*innen"
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
        fun `converts bullet point character into Markdown compatible asterisk`() {
            assertThat("•\tSome content".sanitizeMarkdown()).isEqualTo("* Some content")
        }

        @Test
        fun `converts bullet point character followed by space or no-break space`() {
            assertThat("• One\n•\u00A0Two".sanitizeMarkdown()).isEqualTo("* One\n* Two")
        }

        @Test
        fun `limits indentation of bullet points so that they interrupt a paragraph`() {
            val s = "Badge with:\n    • Secure element\n    • Microcontroller"
            assertThat(s.sanitizeMarkdown()).isEqualTo("Badge with:\n   * Secure element\n   * Microcontroller")
        }

        @Test
        fun `keeps indentation of bullet points nested in an ordered list`() {
            val s = "1. Introduction\n   •\tArtists\n   •\tMotivation"
            assertThat(s.sanitizeMarkdown()).isEqualTo("1. Introduction\n   * Artists\n   * Motivation")
        }

        @Test
        fun `keeps bullet point character within a line untouched`() {
            val s = "Foo • bar"
            assertThat(s.sanitizeMarkdown()).isEqualTo(s)
        }

        @Test
        fun `keeps empty input empty`() {
            assertThat("".sanitizeMarkdown()).isEmpty()
        }
    }

}
