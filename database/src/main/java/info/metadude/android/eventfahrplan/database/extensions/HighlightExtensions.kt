package info.metadude.android.eventfahrplan.database.extensions

import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_OFF
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_ON
import info.metadude.android.eventfahrplan.database.models.Highlight

fun Highlight.toContentValues() = contentValuesOf(
        SESSION_ID to sessionId,
        HIGHLIGHT to if (isHighlight) HIGHLIGHT_STATE_ON else HIGHLIGHT_STATE_OFF
)
