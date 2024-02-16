package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.models.Highlight
import org.junit.jupiter.api.Test

class HighlightExtensionsTest {

    @Test
    fun toContentValues() {
        val highlight = Highlight(
                sessionId = 2342,
                isHighlight = true
        )
        val values = highlight.toContentValues()
        assertThat(values.getAsInteger(SESSION_ID)).isEqualTo(2342)
        assertThat(values.getAsBoolean(HIGHLIGHT)).isEqualTo(true)
    }

}
