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
                sessionId = "be9979c4-18bc-52bb-9480-2c0ac2782c37",
                isHighlight = true
        )
        val values = highlight.toContentValues()
        assertThat(values.getAsString(SESSION_ID)).isEqualTo("be9979c4-18bc-52bb-9480-2c0ac2782c37")
        assertThat(values.getAsBoolean(HIGHLIGHT)).isEqualTo(true)
    }

}
