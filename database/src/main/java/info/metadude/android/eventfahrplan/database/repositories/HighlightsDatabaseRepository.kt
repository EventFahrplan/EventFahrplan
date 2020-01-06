package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.EVENT_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_ON
import info.metadude.android.eventfahrplan.database.extensions.*
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper

class HighlightsDatabaseRepository(

        private val sqLiteOpenHelper: HighlightDBOpenHelper

) {

    fun insert(values: ContentValues, eventId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(HighlightsTable.NAME, EVENT_ID, eventId)
        }, {
            insert(HighlightsTable.NAME, values)
        })
    }

    fun query(): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        val database = sqLiteOpenHelper.readableDatabase
        val cursor: Cursor
        try {
            cursor = database.read(HighlightsTable.NAME)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return highlights.toList()
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val eventIdString = cursor.getString(EVENT_ID)
            val eventId = Integer.parseInt(eventIdString)
            val highlightState = cursor.getInt(HIGHLIGHT)
            val isHighlighted = highlightState == HIGHLIGHT_STATE_ON
            val highlight = Highlight(eventId, isHighlighted)
            highlights.add(highlight)
            cursor.moveToNext()
        }
        cursor.close()

        return highlights.toList()
    }

    fun queryByEventId(eventId: Int): Highlight? {
        val database = sqLiteOpenHelper.readableDatabase
        val cursor = try {
            database.read(
                tableName = HighlightsTable.NAME,
                selection = "$EVENT_ID=?",
                selectionArgs = arrayOf(eventId.toString())
            )
        } catch (e: SQLiteException) {
            return null
        }

        val highlight = cursor.use {
            if (cursor.moveToFirst()) {
                val highlightState = cursor.getInt(HIGHLIGHT)
                val isHighlighted = highlightState == HIGHLIGHT_STATE_ON
                Highlight(eventId, isHighlighted)
            } else {
                null
            }
        }

        return highlight
    }

}
