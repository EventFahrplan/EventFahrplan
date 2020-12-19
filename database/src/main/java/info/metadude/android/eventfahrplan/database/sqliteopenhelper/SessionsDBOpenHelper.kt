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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.GUID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LANG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LINKS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_LICENSE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REL_START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_IDENTIFIER
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_INDEX
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.GUID
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
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "sessions"

        // language=sql
        const val SESSIONS_TABLE_CREATE = "CREATE TABLE ${SessionsTable.NAME} (" +
                "$GUID TEXT NOT NULL UNIQUE, " +
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
                    "$_ID INTEGER PRIMARY KEY AUTOINCREMENT, ${SessionByNotificationIdTable.Columns.GUID} TEXT)"

        // language=sql
        const val SCHEDULE_STATISTIC_VIEW_CREATE =
            "CREATE VIEW IF NOT EXISTS ${StatisticsView.NAME} AS SELECT " +
                    "COUNT(CASE WHEN $TITLE IS NULL OR $TITLE = '' THEN 1 END) AS $TITLE_NONE, " +
                    "COUNT(CASE WHEN $TITLE IS NOT NULL AND $TITLE != '' THEN 1 END) AS $TITLE_PRESENT, " +
                    "COUNT(CASE WHEN $SUBTITLE IS NULL OR $SUBTITLE = '' THEN 1 END) AS $SUBTITLE_NONE, " +
                    "COUNT(CASE WHEN $SUBTITLE IS NOT NULL AND $SUBTITLE != '' THEN 1 END) AS $SUBTITLE_PRESENT, " +
                    "COUNT(CASE WHEN $DAY IS NULL OR $DAY = '' THEN 1 END) AS $DAY_INDEX_NONE, " +
                    "COUNT(CASE WHEN $DAY IS NOT NULL AND $DAY != '' THEN 1 END) AS $DAY_INDEX_PRESENT, " +
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
    }

}

private fun SQLiteDatabase.addIntegerColumn(columnName: String, default: Int?) {
    addIntegerColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

private fun SQLiteDatabase.addTextColumn(columnName: String, default: String?) {
    addTextColumn(tableName = SessionsTable.NAME, columnName = columnName, default = default)
}

