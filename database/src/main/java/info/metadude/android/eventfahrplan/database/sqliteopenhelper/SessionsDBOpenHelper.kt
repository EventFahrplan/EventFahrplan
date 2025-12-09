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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DAY_INDEX
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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ABSTRACT_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ABSTRACT_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DATE_TEXT_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DATE_TEXT_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DATE_UTC_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DATE_UTC_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DAY_INDEX_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DAY_INDEX_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DESCRIPTION_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DESCRIPTION_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DURATION_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.DURATION_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.FEEDBACK_URL_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.FEEDBACK_URL_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.LANGUAGES_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.LANGUAGES_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.LINKS_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.LINKS_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RECORDING_LICENSE_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RECORDING_LICENSE_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RECORDING_OPTOUT_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RECORDING_OPTOUT_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RELATIVE_START_TIME_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.RELATIVE_START_TIME_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_IDENTIFIER_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_IDENTIFIER_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_INDEX_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_INDEX_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_NAME_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.ROOM_NAME_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SLUG_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SLUG_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SPEAKERS_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SPEAKERS_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.START_TIME_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.START_TIME_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SUBTITLE_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.SUBTITLE_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TIME_ZONE_OFFSET_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TIME_ZONE_OFFSET_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TITLE_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TITLE_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TRACK_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TRACK_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TYPE_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.TYPE_PRESENT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.URL_NONE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.StatisticsView.Columns.URL_PRESENT
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
        const val DATABASE_VERSION = 18
        const val DATABASE_NAME = "lectures" // Keep table name to avoid database migration.

        // language=sql
        const val SESSIONS_TABLE_CREATE = "CREATE TABLE ${SessionsTable.NAME} (" +
                "$SESSION_ID TEXT, " +
                "$TITLE TEXT, " +
                "$SUBTITLE TEXT, " +
                "$DAY_INDEX INTEGER, " +
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

        // language=sql
        const val SCHEDULE_STATISTIC_VIEW_CREATE =
            "CREATE VIEW IF NOT EXISTS ${StatisticsView.NAME} AS SELECT " +
                    "COUNT(CASE WHEN $TITLE IS NULL OR $TITLE = '' THEN 1 END) AS $TITLE_NONE, " +
                    "COUNT(CASE WHEN $TITLE IS NOT NULL AND $TITLE != '' THEN 1 END) AS $TITLE_PRESENT, " +
                    "COUNT(CASE WHEN $SUBTITLE IS NULL OR $SUBTITLE = '' THEN 1 END) AS $SUBTITLE_NONE, " +
                    "COUNT(CASE WHEN $SUBTITLE IS NOT NULL AND $SUBTITLE != '' THEN 1 END) AS $SUBTITLE_PRESENT, " +
                    "COUNT(CASE WHEN $DAY_INDEX IS NULL OR $DAY_INDEX = '' OR $DAY_INDEX = 0 THEN 1 END) AS $DAY_INDEX_NONE, " +
                    "COUNT(CASE WHEN $DAY_INDEX IS NOT NULL AND $DAY_INDEX != '' AND $DAY_INDEX != 0 THEN 1 END) AS $DAY_INDEX_PRESENT, " +
                    "COUNT(CASE WHEN $ROOM_NAME IS NULL OR $ROOM_NAME = '' THEN 1 END) AS $ROOM_NAME_NONE, " +
                    "COUNT(CASE WHEN $ROOM_NAME IS NOT NULL AND $ROOM_NAME != '' THEN 1 END) AS $ROOM_NAME_PRESENT, " +
                    "COUNT(CASE WHEN $START IS NULL OR $START = '' THEN 1 END) AS $START_TIME_NONE, " +
                    "COUNT(CASE WHEN $START IS NOT NULL AND $START != '' THEN 1 END) AS $START_TIME_PRESENT, " +
                    "COUNT(CASE WHEN $DURATION IS NULL OR $DURATION = '' THEN 1 END) AS $DURATION_NONE, " +
                    "COUNT(CASE WHEN $DURATION IS NOT NULL AND $DURATION != '' THEN 1 END) AS $DURATION_PRESENT, " +
                    "COUNT(CASE WHEN $SPEAKERS IS NULL OR $SPEAKERS = '' THEN 1 END) AS $SPEAKERS_NONE, " +
                    "COUNT(CASE WHEN $SPEAKERS IS NOT NULL AND $SPEAKERS != '' THEN 1 END) AS $SPEAKERS_PRESENT, " +
                    "COUNT(CASE WHEN $TRACK IS NULL OR $TRACK = '' THEN 1 END) AS $TRACK_NONE, " +
                    "COUNT(CASE WHEN $TRACK IS NOT NULL AND $TRACK != '' THEN 1 END) AS $TRACK_PRESENT, " +
                    "COUNT(CASE WHEN $TYPE IS NULL OR $TYPE = '' THEN 1 END) AS $TYPE_NONE, " +
                    "COUNT(CASE WHEN $TYPE IS NOT NULL AND $TYPE != '' THEN 1 END) AS $TYPE_PRESENT, " +
                    "COUNT(CASE WHEN $LANG IS NULL OR $LANG = '' THEN 1 END) AS $LANGUAGES_NONE, " +
                    "COUNT(CASE WHEN $LANG IS NOT NULL AND $LANG != '' THEN 1 END) AS $LANGUAGES_PRESENT, " +
                    "COUNT(CASE WHEN $ABSTRACT IS NULL OR $ABSTRACT = '' THEN 1 END) AS $ABSTRACT_NONE, " +
                    "COUNT(CASE WHEN $ABSTRACT IS NOT NULL AND $ABSTRACT != '' THEN 1 END) AS $ABSTRACT_PRESENT, " +
                    "COUNT(CASE WHEN $DESCR IS NULL OR $DESCR = '' THEN 1 END) AS $DESCRIPTION_NONE, " +
                    "COUNT(CASE WHEN $DESCR IS NOT NULL AND $DESCR != '' THEN 1 END) AS $DESCRIPTION_PRESENT, " +
                    "COUNT(CASE WHEN $REL_START IS NULL OR $REL_START = '' THEN 1 END) AS $RELATIVE_START_TIME_NONE, " +
                    "COUNT(CASE WHEN $REL_START IS NOT NULL AND $REL_START != '' THEN 1 END) AS $RELATIVE_START_TIME_PRESENT, " +
                    "COUNT(CASE WHEN $DATE_TEXT IS NULL OR $DATE_TEXT = '' THEN 1 END) AS $DATE_TEXT_NONE, " +
                    "COUNT(CASE WHEN $DATE_TEXT IS NOT NULL AND $DATE_TEXT != '' THEN 1 END) AS $DATE_TEXT_PRESENT, " +
                    "COUNT(CASE WHEN $LINKS IS NULL OR $LINKS = '' THEN 1 END) AS $LINKS_NONE, " +
                    "COUNT(CASE WHEN $LINKS IS NOT NULL AND $LINKS != '' THEN 1 END) AS $LINKS_PRESENT, " +
                    "COUNT(CASE WHEN $DATE_UTC IS NULL OR $DATE_UTC = '' THEN 1 END) AS $DATE_UTC_NONE, " +
                    "COUNT(CASE WHEN $DATE_UTC IS NOT NULL AND $DATE_UTC != '' THEN 1 END) AS $DATE_UTC_PRESENT, " +
                    "COUNT(CASE WHEN $ROOM_INDEX IS NULL OR $ROOM_INDEX = '' THEN 1 END) AS $ROOM_INDEX_NONE, " +
                    "COUNT(CASE WHEN $ROOM_INDEX IS NOT NULL AND $ROOM_INDEX != '' THEN 1 END) AS $ROOM_INDEX_PRESENT, " +
                    "COUNT(CASE WHEN $REC_LICENSE IS NULL OR $REC_LICENSE = '' THEN 1 END) AS $RECORDING_LICENSE_NONE, " +
                    "COUNT(CASE WHEN $REC_LICENSE IS NOT NULL AND $REC_LICENSE != '' THEN 1 END) AS $RECORDING_LICENSE_PRESENT, " +
                    "COUNT(CASE WHEN $REC_OPTOUT IS NULL OR $REC_OPTOUT = '' THEN 1 END) AS $RECORDING_OPTOUT_NONE, " +
                    "COUNT(CASE WHEN $REC_OPTOUT IS NOT NULL AND $REC_OPTOUT != '' THEN 1 END) AS $RECORDING_OPTOUT_PRESENT, " +
                    "COUNT(CASE WHEN $SLUG IS NULL OR $SLUG = '' THEN 1 END) AS $SLUG_NONE, " +
                    "COUNT(CASE WHEN $SLUG IS NOT NULL AND $SLUG != '' THEN 1 END) AS $SLUG_PRESENT, " +
                    "COUNT(CASE WHEN $URL IS NULL OR $URL = '' THEN 1 END) AS $URL_NONE, " +
                    "COUNT(CASE WHEN $URL IS NOT NULL AND $URL != '' THEN 1 END) AS $URL_PRESENT, " +
                    "COUNT(CASE WHEN $TIME_ZONE_OFFSET IS NULL OR $TIME_ZONE_OFFSET = '' THEN 1 END) AS $TIME_ZONE_OFFSET_NONE, " +
                    "COUNT(CASE WHEN $TIME_ZONE_OFFSET IS NOT NULL AND $TIME_ZONE_OFFSET != '' THEN 1 END) AS $TIME_ZONE_OFFSET_PRESENT, " +
                    "COUNT(CASE WHEN $ROOM_IDENTIFIER IS NULL OR $ROOM_IDENTIFIER = '' THEN 1 END) AS $ROOM_IDENTIFIER_NONE, " +
                    "COUNT(CASE WHEN $ROOM_IDENTIFIER IS NOT NULL AND $ROOM_IDENTIFIER != '' THEN 1 END) AS $ROOM_IDENTIFIER_PRESENT, " +
                    "COUNT(CASE WHEN $FEEDBACK_URL IS NULL OR $FEEDBACK_URL = '' THEN 1 END) AS $FEEDBACK_URL_NONE, " +
                    "COUNT(CASE WHEN $FEEDBACK_URL IS NOT NULL AND $FEEDBACK_URL != '' THEN 1 END) AS $FEEDBACK_URL_PRESENT " +
                    "FROM ${SessionsTable.NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        transaction {
            execSQL(SESSIONS_TABLE_CREATE)
            execSQL(SESSION_BY_NOTIFICATION_ID_TABLE_CREATE)
            execSQL(SCHEDULE_STATISTIC_VIEW_CREATE)
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
        if (oldVersion < 16) {
            execSQL(SCHEDULE_STATISTIC_VIEW_CREATE)
        }
        if (oldVersion < 17) {
            // Clear database from Camp 2023 & 37C3 2023.
            dropTableIfExist(SessionsTable.NAME)
            dropTableIfExist(SessionByNotificationIdTable.NAME)
            onCreate(this)
        }
        if (oldVersion < 18) {
            // Clear database from 38C3 2024.
            dropTableIfExist(SessionsTable.NAME)
            dropTableIfExist(SessionByNotificationIdTable.NAME)
            onCreate(this)
        }
    }

}

private fun SQLiteDatabase.addIntegerColumn(columnName: String, default: Int?) {
    addIntegerColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

private fun SQLiteDatabase.addTextColumn(columnName: String, default: String?) {
    addTextColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

