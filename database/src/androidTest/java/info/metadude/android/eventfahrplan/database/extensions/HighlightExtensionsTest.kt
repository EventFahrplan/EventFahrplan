package info.metadude.android.eventfahrplan.database.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.models.Highlight
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
