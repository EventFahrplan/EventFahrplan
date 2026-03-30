package info.metadude.android.eventfahrplan.commons.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `blank input becomes empty`() {
            assertThat("   ".sanitize()).isEmpty()
        }

        @Test
        fun `null input becomes empty`() {
            assertThat(null.sanitize()).isEmpty()
        }
    }

}
