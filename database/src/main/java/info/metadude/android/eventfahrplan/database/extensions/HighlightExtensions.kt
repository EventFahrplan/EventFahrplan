package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_OFF
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_ON
import info.metadude.android.eventfahrplan.database.models.Highlight

fun Highlight.toContentValues() = ContentValues().apply {
    put(SESSION_ID, sessionId)
    put(HIGHLIGHT, if (isHighlight) HIGHLIGHT_STATE_ON else HIGHLIGHT_STATE_OFF)
}
