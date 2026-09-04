package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.FRAB
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PENTABARF
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PRETALX
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServerBackendTypeTest {

    @Nested
    inner class Of {

        @Test
        fun `of returns PENTABARF for pentabarf`() {
            val type = ServerBackendType.of("pentabarf")
            assertThat(type).isEqualTo(PENTABARF)
        }

        @Test
        fun `of returns FRAB for frab`() {
            val type = ServerBackendType.of("frab")
            assertThat(type).isEqualTo(FRAB)
        }

        @Test
        fun `of returns PRETALX for pretalx`() {
            val type = ServerBackendType.of("pretalx")
            assertThat(type).isEqualTo(PRETALX)
        }

        @Test
        fun `of throws error for unknown server backend type string`() {
            val exception = assertThrows<UnknownServerBackendTypeException> {
                ServerBackendType.of("unknown")
            }
            assertThat(exception).hasMessageThat().isEqualTo("""Unknown server backend type: "unknown".""")
        }

        @Test
        fun `of throws error for empty server backend type string`() {
            val exception = assertThrows<UnknownServerBackendTypeException> {
                ServerBackendType.of("")
            }
            assertThat(exception).hasMessageThat().isEqualTo("""Unknown server backend type: "".""")
        }

    }

    @Nested
    inner class MarkupLanguage {

        @Test
        fun `PENTABARF supports HTML markup`() {
            assertThat(PENTABARF.markupLanguage).isEqualTo(Html)
        }

        @Test
        fun `FRAB supports Markdown markup`() {
            assertThat(FRAB.markupLanguage).isEqualTo(Markdown)
        }

        @Test
        fun `PRETALX supports Markdown markup`() {
            assertThat(PRETALX.markupLanguage).isEqualTo(Markdown)
        }

    }

}
