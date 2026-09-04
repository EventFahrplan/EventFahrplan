package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Markdown
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.FRAB
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.OPENKI
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PENTABARF
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.PRETALX
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType.WAFER
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
        fun `of returns WAFER for wafer`() {
            val type = ServerBackendType.of("wafer")
            assertThat(type).isEqualTo(WAFER)
        }

        @Test
        fun `of returns OPENKI for openki`() {
            val type = ServerBackendType.of("openki")
            assertThat(type).isEqualTo(OPENKI)
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

        @Test
        fun `WAFER supports Markdown markup`() {
            assertThat(WAFER.markupLanguage).isEqualTo(Markdown)
        }

        @Test
        fun `OPENKI supports Markdown markup`() {
            assertThat(OPENKI.markupLanguage).isEqualTo(Markdown)
        }

    }

}
