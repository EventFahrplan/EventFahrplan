package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.GUID
import info.metadude.android.eventfahrplan.database.models.Highlight
import org.junit.jupiter.api.Test

class HighlightExtensionsTest {

    @Test
    fun toContentValues() {
        val highlight = Highlight(
                guid = "11111111-1111-1111-1111-111111112342",
                isHighlight = true
        )
        val values = highlight.toContentValues()
        assertThat(values.getAsInteger(GUID)).isEqualTo(2342)
        assertThat(values.getAsBoolean(HIGHLIGHT)).isEqualTo(true)
    }

}
