package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.core.database.sqlite.transaction
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_OFF
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Values.HIGHLIGHT_STATE_ON
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.map
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.updateRows
import info.metadude.android.eventfahrplan.database.extensions.upsert
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper

class RealHighlightsDatabaseRepository(

        private val sqLiteOpenHelper: HighlightDBOpenHelper

) : HighlightsDatabaseRepository {

    override fun update(values: ContentValues, sessionId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(HighlightsTable.NAME, SESSION_ID, sessionId)
        }, {
            insert(HighlightsTable.NAME, values)
        })
    }

    override fun query(): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.read(HighlightsTable.NAME, orderBy = SESSION_ID)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return highlights.toList()
        }

        return cursor.map {
            val sessionIdString = cursor.getString(SESSION_ID)
            val sessionId = Integer.parseInt(sessionIdString)
            val highlightState = cursor.getInt(HIGHLIGHT)
            val isHighlighted = highlightState == HIGHLIGHT_STATE_ON

            Highlight(sessionId, isHighlighted)
        }
    }

    override fun queryBySessionId(sessionId: Int): Highlight? {
        val database = sqLiteOpenHelper.readableDatabase
        val cursor = try {
            database.read(
                tableName = HighlightsTable.NAME,
                selection = "$SESSION_ID=?",
                selectionArgs = arrayOf(sessionId.toString())
            )
        } catch (e: SQLiteException) {
            return null
        }

        return cursor.use {
            if (cursor.moveToFirst()) {
                val highlightState = cursor.getInt(HIGHLIGHT)
                val isHighlighted = highlightState == HIGHLIGHT_STATE_ON
                Highlight(sessionId, isHighlighted)
            } else {
                null
            }
        }
    }

    /**
     * Resets the value of the [HIGHLIGHT] column to [`false`][HIGHLIGHT_STATE_OFF] for each row.
     * Rows are not removed.
     */
    override fun deleteAll() = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            updateRows(
                    tableName = HighlightsTable.NAME,
                    contentValues = ContentValues().apply {
                        put(HIGHLIGHT, HIGHLIGHT_STATE_OFF)
                    })
        }
    }

}
