package info.metadude.android.eventfahrplan.database.sqliteopenhelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.IS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.NAME
import info.metadude.android.eventfahrplan.database.extensions.dropTableIfExist

internal class FavoritesDBOpenHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    private companion object {
        const val DATABASE_VERSION = 7
        const val DATABASE_NAME = "highlight"

        // language=sql
        const val FAVORITES_TABLE_CREATE = "CREATE TABLE $NAME (" +
                "$ID INTEGER PRIMARY KEY, " +
                "$SESSION_ID INTEGER, " +
                "$IS_FAVORITE INTEGER" +
                ");"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(FAVORITES_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = with(db) {
        // Clear database from Camp 2023 & 38C3 2024.
        dropTableIfExist(NAME)
        onCreate(this)
    }

}
