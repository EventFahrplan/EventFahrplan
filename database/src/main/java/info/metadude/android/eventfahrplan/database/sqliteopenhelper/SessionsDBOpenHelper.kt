package info.metadude.android.eventfahrplan.database.sqliteopenhelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns._ID
import androidx.core.database.sqlite.transaction
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionByNotificationIdTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ABSTRACT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DAY_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_CANCELED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_NEW
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_LANGUAGE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_RECORDING_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_ROOM_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_START_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_TEXT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_UTC
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DESCR
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.FEEDBACK_URL
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LANG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LINKS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_LICENSE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REL_START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_IDENTIFIER
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SLUG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TIME_ZONE_OFFSET
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.TYPE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.URL
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Defaults.DATE_UTC_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Defaults.ROOM_IDX_DEFAULT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.extensions.addIntegerColumn
import info.metadude.android.eventfahrplan.database.extensions.addTextColumn
import info.metadude.android.eventfahrplan.database.extensions.columnExists
import info.metadude.android.eventfahrplan.database.extensions.dropTableIfExist

internal class SessionsDBOpenHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    private companion object {
        const val DATABASE_VERSION = 15
        const val DATABASE_NAME = "lectures" // Keep table name to avoid database migration.

        // language=sql
        const val SESSIONS_TABLE_CREATE = "CREATE TABLE ${SessionsTable.NAME} (" +
                "$SESSION_ID TEXT, " +
                "$TITLE TEXT, " +
                "$SUBTITLE TEXT, " +
                "$DAY INTEGER, " +
                "$ROOM_NAME TEXT, " +
                "$ROOM_IDENTIFIER TEXT DEFAULT '', " +
                "$SLUG TEXT, " +
                "$START INTEGER, " +
                "$DURATION INTEGER, " +
                "$FEEDBACK_URL TEXT DEFAULT NULL, " +
                "$SPEAKERS TEXT, " +
                "$TRACK TEXT, " +
                "$TYPE TEXT, " +
                "$LANG TEXT, " +
                "$ABSTRACT TEXT, " +
                "$DESCR TEXT, " +
                "$REL_START INTEGER, " +
                "$DATE_TEXT TEXT, " +
                "$LINKS TEXT, " +
                "$DATE_UTC INTEGER, " +
                "$TIME_ZONE_OFFSET INTEGER DEFAULT NULL, " +
                "$ROOM_INDEX INTEGER, " +
                "$REC_LICENSE TEXT, " +
                "$REC_OPTOUT INTEGER, " +
                "$URL TEXT DEFAULT '', " +
                "$CHANGED_TITLE INTEGER, " +
                "$CHANGED_SUBTITLE INTEGER, " +
                "$CHANGED_ROOM_NAME INTEGER, " +
                "$CHANGED_DAY_INDEX INTEGER, " +
                "$CHANGED_SPEAKERS INTEGER, " +
                "$CHANGED_RECORDING_OPTOUT INTEGER, " +
                "$CHANGED_LANGUAGE INTEGER, " +
                "$CHANGED_TRACK INTEGER, " +
                "$CHANGED_IS_NEW INTEGER, " +
                "$CHANGED_START_TIME INTEGER, " +
                "$CHANGED_DURATION INTEGER, " +
                "$CHANGED_IS_CANCELED INTEGER" +
                ");"

        /**
         * Create statement for a mapping table (notification ID, session ID). Each insert
         * automatically increments the primary key and therefore generates a new notification ID.
         */
        // language=sql
        const val SESSION_BY_NOTIFICATION_ID_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS ${SessionByNotificationIdTable.NAME} (" +
                    "$_ID INTEGER PRIMARY KEY AUTOINCREMENT, ${SessionByNotificationIdTable.Columns.SESSION_ID} TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        transaction {
            execSQL(SESSIONS_TABLE_CREATE)
            execSQL(SESSION_BY_NOTIFICATION_ID_TABLE_CREATE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = with(db) {
        if (oldVersion < 2 && newVersion >= 2) {
            addIntegerColumn(DATE_UTC, default = DATE_UTC_DEFAULT)
        }
        if (oldVersion < 3 && newVersion >= 3) {
            addIntegerColumn(ROOM_INDEX, default = ROOM_IDX_DEFAULT)
        }
        if (oldVersion < 4 && newVersion >= 4) {
            addTextColumn(REC_LICENSE, default = "")
            addIntegerColumn(REC_OPTOUT, default = REC_OPT_OUT_OFF)
        }
        if (oldVersion < 5 && newVersion >= 5) {
            addIntegerColumn(CHANGED_TITLE, default = 0)
            addIntegerColumn(CHANGED_SUBTITLE, default = 0)
            addIntegerColumn(CHANGED_ROOM_NAME, default = 0)
            addIntegerColumn(CHANGED_DAY_INDEX, default = 0)
            addIntegerColumn(CHANGED_SPEAKERS, default = 0)
            addIntegerColumn(CHANGED_RECORDING_OPTOUT, default = 0)
            addIntegerColumn(CHANGED_LANGUAGE, default = 0)
            addIntegerColumn(CHANGED_TRACK, default = 0)
            addIntegerColumn(CHANGED_IS_NEW, default = 0)
            addIntegerColumn(CHANGED_START_TIME, default = 0)
            addIntegerColumn(CHANGED_DURATION, default = 0)
            addIntegerColumn(CHANGED_IS_CANCELED, default = 0)
        }
        if (oldVersion < 6 && newVersion >= 6) {
            addTextColumn(SLUG, default = "")
        }
        if (oldVersion < 7 && newVersion >= 7) {
            addTextColumn(URL, default = "")
        }
        if (oldVersion < 8) {
            // Clear database from 34C3.
            dropTableIfExist(SessionsTable.NAME)
            onCreate(this)
        }
        if (oldVersion < 9) {
            // Clear database from 35C3 & Camp 2019.
            dropTableIfExist(SessionsTable.NAME)
            onCreate(this)
        }
        if (oldVersion < 10 && newVersion >= 10) {
            execSQL(SESSION_BY_NOTIFICATION_ID_TABLE_CREATE)
        }
        if (oldVersion < 11 && newVersion >= 11) {
            if (!columnExists(SessionsTable.NAME, TIME_ZONE_OFFSET)) {
                addIntegerColumn(TIME_ZONE_OFFSET, default = null)
            }
        }
        if (oldVersion < 12) {
            // Clear database from rC3 12/2020.
            dropTableIfExist(SessionsTable.NAME)
            dropTableIfExist(SessionByNotificationIdTable.NAME)
            onCreate(this)
        }
        if (oldVersion < 13) {
            // Clear database from rC3 NOWHERE 12/2021 & 36C3 2019.
            dropTableIfExist(SessionsTable.NAME)
            dropTableIfExist(SessionByNotificationIdTable.NAME)
            onCreate(this)
        }
        if (oldVersion < 14) {
            if (!columnExists(SessionsTable.NAME, ROOM_IDENTIFIER)) {
                addTextColumn(ROOM_IDENTIFIER, default = "")
            }
        }
        if (oldVersion < 15) {
            if (!columnExists(SessionsTable.NAME, FEEDBACK_URL)) {
                addTextColumn(FEEDBACK_URL, default = null)
            }
        }
    }

}

private fun SQLiteDatabase.addIntegerColumn(columnName: String, default: Int?) {
    addIntegerColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

private fun SQLiteDatabase.addTextColumn(columnName: String, default: String?) {
    addTextColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

