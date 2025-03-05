package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Html
import nerd.tuxmobil.fahrplan.congress.models.MarkupLanguage.Markdown
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServerBackendTypeTest {

    @Test
    fun `getMarkupLanguage returns Html for PENTABARF`() {
        val markupLanguage = ServerBackendType.getMarkupLanguage("pentabarf")
        assertThat(markupLanguage).isEqualTo(Html)
    }

    @Test
    fun `getMarkupLanguage returns Markdown for FRAB`() {
        val markupLanguage = ServerBackendType.getMarkupLanguage("frab")
        assertThat(markupLanguage).isEqualTo(Markdown)
    }

    @Test
    fun `getMarkupLanguage returns Markdown for PRETALX`() {
        val markupLanguage = ServerBackendType.getMarkupLanguage("pretalx")
        assertThat(markupLanguage).isEqualTo(Markdown)
    }

    @Test
    fun `getMarkupLanguage throws error for unknown server backend type`() {
        val exception = assertThrows<IllegalStateException> {
            ServerBackendType.getMarkupLanguage("unknown")
        }
        assertThat(exception).hasMessageThat().isEqualTo("""Unknown server backend type: "unknown".""")
    }

    @Test
    fun `getMarkupLanguage throws error for empty string backend type`() {
        val exception = assertThrows<IllegalStateException> {
            ServerBackendType.getMarkupLanguage("")
        }
        assertThat(exception).hasMessageThat().isEqualTo("""Unknown server backend type: "".""")
    }

}
