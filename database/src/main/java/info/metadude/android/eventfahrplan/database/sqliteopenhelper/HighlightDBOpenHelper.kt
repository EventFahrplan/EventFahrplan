package info.metadude.android.eventfahrplan.database.sqliteopenhelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.HIGHLIGHT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.NAME
import info.metadude.android.eventfahrplan.database.extensions.dropTableIfExist

class HighlightDBOpenHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    private companion object {
        const val DATABASE_VERSION = 5
        const val DATABASE_NAME = "highlight"

        // language=sql
        const val HIGHLIGHT_TABLE_CREATE = "CREATE TABLE $NAME (" +
                "$ID INTEGER PRIMARY KEY, " +
                "$SESSION_ID INTEGER, " +
                "$HIGHLIGHT INTEGER" +
                ");"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(HIGHLIGHT_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = with(db) {
        // Clear database from 36C3 2019.
        dropTableIfExist(NAME)
        onCreate(this)
    }

}
