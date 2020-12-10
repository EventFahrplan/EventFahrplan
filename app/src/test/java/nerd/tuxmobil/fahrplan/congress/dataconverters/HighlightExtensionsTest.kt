package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Highlight
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel

class HighlightExtensionsTest {

    @Test
    fun toHighlightAppModel() {
        val highlightDatabaseModel = HighlightDatabaseModel(
                sessionId = "11111111-1111-1111-1111-111111111111",
                isHighlight = true
        )
        val highlightAppModel = Highlight(
                sessionId = "11111111-1111-1111-1111-111111111111",
                isHighlight = true
        )
        assertThat(highlightDatabaseModel.toHighlightAppModel()).isEqualTo(highlightAppModel)
    }

}
