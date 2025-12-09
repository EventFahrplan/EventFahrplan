package info.metadude.android.eventfahrplan.database.sqliteopenhelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults.ETAG_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.NAME
import info.metadude.android.eventfahrplan.database.extensions.addTextColumn
import info.metadude.android.eventfahrplan.database.extensions.columnExists
import info.metadude.android.eventfahrplan.database.extensions.dropTableIfExist

internal class MetaDBOpenHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    private companion object {
        const val DATABASE_VERSION = 12
        const val DATABASE_NAME = "meta"

        // language=sql
        const val META_TABLE_CREATE = "CREATE TABLE $NAME (" +
                "$NUM_DAYS INTEGER, " +
                "$VERSION TEXT, " +
                "$TITLE TEXT, " +
                "$SUBTITLE TEXT, " +
                "$SCHEDULE_ETAG TEXT, " +
                "$TIME_ZONE_NAME TEXT, " +
                "$SCHEDULE_LAST_MODIFIED TEXT DEFAULT ''," +
                "$SCHEDULE_GENERATOR_NAME TEXT DEFAULT NULL," +
                "$SCHEDULE_GENERATOR_VERSION TEXT DEFAULT NULL" +
                ");"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(META_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = with(db) {
        if (oldVersion < 3 && newVersion >= 3) {
            addTextColumn(SCHEDULE_ETAG, default = ETAG_DEFAULT)
        }
        if (oldVersion < 6 && newVersion >= 6) {
            addTextColumn(TIME_ZONE_NAME, default = null)
        }
        if (oldVersion < 4) {
            // Clear database from 34C3.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 5) {
            // Clear database from 35C3 & Camp 2019.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 7) {
            // Clear database from rC3 12/2020.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 8) {
            // Clear database from rC3 NOWHERE 12/2021 & 36C3 2019.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 9) {
            if (!columnExists(NAME, SCHEDULE_LAST_MODIFIED)) {
                addTextColumn(SCHEDULE_LAST_MODIFIED, default = "")
            }
        }
        if (oldVersion < 10) {
            // Clear database from Camp 2023 & 37C3 2023.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 11) {
            if (!columnExists(NAME, SCHEDULE_GENERATOR_NAME)) {
                addTextColumn(SCHEDULE_GENERATOR_NAME, default = null)
            }
            if (!columnExists(NAME, SCHEDULE_GENERATOR_VERSION)) {
                addTextColumn(SCHEDULE_GENERATOR_VERSION, default = null)
            }
        }
        if (oldVersion < 12) {
            // Clear database from 38C3 2024.
            dropTableIfExist(NAME)
            onCreate(this)
        }
    }

}

private fun SQLiteDatabase.addTextColumn(columnName: String, default: String?) {
    addTextColumn(tableName = NAME, columnName = columnName, default = default)
}
