package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.EVENT_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_ON
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper

class HighlightsDatabaseRepository(

        private val sqLiteOpenHelper: HighlightDBOpenHelper

) {

    fun insert(values: ContentValues, eventId: String) = with(sqLiteOpenHelper.writableDatabase) {
        try {
            beginTransaction()
            delete(HighlightsTable.NAME, EVENT_ID, eventId)
            insert(HighlightsTable.NAME, values)
            setTransactionSuccessful()
        } catch (ignore: SQLException) {
            // Fail silently
        } finally {
            endTransaction()
            close()
            sqLiteOpenHelper.close()
        }
    }

    fun query(): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        val database = sqLiteOpenHelper.readableDatabase
        val cursor: Cursor
        try {
            cursor = database.read(HighlightsTable.NAME)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            database.close()
            sqLiteOpenHelper.close()
            return highlights.toList()
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val eventIdString = cursor.getString(cursor.getColumnIndex(EVENT_ID))
            val eventId = Integer.parseInt(eventIdString)
            val highlightState = cursor.getInt(cursor.getColumnIndex(HIGHLIGHT))
            val isHighlighted = highlightState == HIGHLIGHT_STATE_ON
            val highlight = Highlight(eventId, isHighlighted)
            highlights.add(highlight)
            cursor.moveToNext()
        }
        cursor.close()
        database.close()
        sqLiteOpenHelper.close()

        return highlights.toList()
    }

}
