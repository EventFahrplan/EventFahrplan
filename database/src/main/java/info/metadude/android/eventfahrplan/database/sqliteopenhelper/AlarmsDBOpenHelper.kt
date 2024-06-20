package info.metadude.android.eventfahrplan.database.sqliteopenhelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.ALARM_TIME_IN_MIN
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DISPLAY_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME_TEXT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.NAME
import info.metadude.android.eventfahrplan.database.extensions.addIntegerColumn
import info.metadude.android.eventfahrplan.database.extensions.dropTableIfExist

internal class AlarmsDBOpenHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    private companion object {
        const val DATABASE_VERSION = 6
        const val DATABASE_NAME = "alarms"

        // language=sql
        const val ALARMS_TABLE_CREATE = "CREATE TABLE $NAME (" +
                "$ID INTEGER PRIMARY KEY, " +
                "$SESSION_TITLE TEXT, " +
                "$ALARM_TIME_IN_MIN INTEGER DEFAULT $ALARM_TIME_IN_MIN_DEFAULT, " +
                "$TIME INTEGER, " +
                "$TIME_TEXT TEXT, " +
                "$SESSION_ID INTEGER, " +
                "$DISPLAY_TIME INTEGER, " +
                "$DAY INTEGER" +
                ");"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(ALARMS_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = with(db) {
        if (oldVersion < 2 && newVersion >= 2) {
            addIntegerColumn(tableName = NAME, columnName = ALARM_TIME_IN_MIN, default = ALARM_TIME_IN_MIN_DEFAULT)
        }
        if (oldVersion < 3) {
            // Clear database from 34C3.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 4) {
            // Clear database from 35C3 & Camp 2019.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 5) {
            // Clear database from rC3 12/2020.
            dropTableIfExist(NAME)
            onCreate(this)
        }
        if (oldVersion < 6) {
            // Clear database from rC3 NOWHERE 12/2021 & 36C3 2019.
            dropTableIfExist(NAME)
            onCreate(this)
        }
    }

}
