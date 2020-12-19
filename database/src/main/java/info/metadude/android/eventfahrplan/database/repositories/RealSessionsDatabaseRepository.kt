package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.core.database.sqlite.transaction
import info.metadude.android.eventfahrplan.commons.logging.Logging
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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_START_TIME
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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns._ID
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
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getIntOrNull
import info.metadude.android.eventfahrplan.database.extensions.getLong
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.getStringOrNull
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.map
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.updateRow
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import info.metadude.android.eventfahrplan.database.models.Session
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper

internal class RealSessionsDatabaseRepository(

    private val sqLiteOpenHelper: SessionsDBOpenHelper,
    private val logging: Logging

) : SessionsDatabaseRepository {

    private companion object {
        const val LOG_TAG = "SessionsDatabaseRepository"
    }

    /**
     * Inserts the session ID into the [SessionByNotificationIdTable] and returns
     * the newly generated notification ID which is associated with the session ID.
     *
     * Notification IDs are incremented automatically.
     *
     * [guidContentValues] is expected to be composed from
     * [SessionByNotificationIdTable.Columns.GUID] and the session ID value.
     *
     * See also: [android.database.sqlite.SQLiteDatabase.insert]
     */
    override fun insertGuid(guidContentValues: ContentValues) = with(sqLiteOpenHelper) {
        writableDatabase.insert(SessionByNotificationIdTable.NAME, guidContentValues)
    }.toInt()

    /**
     * Deletes the [SessionByNotificationIdTable] row associated with the given unique [notificationId].
     * Returns the number of affected rows.
     */
    override fun deleteGuidByNotificationId(notificationId: Int) = with(sqLiteOpenHelper) {
        writableDatabase.delete(SessionByNotificationIdTable.NAME, SessionByNotificationIdTable.Columns._ID, "$notificationId")
    }


    /**
     * Updates or inserts sessions based on the given [contentValuesByGuid].
     * Removes all sessions identified by their [session GUIDs][toBeDeletedSessionGuids].
     */
    override fun upsertSessions(
        contentValuesByGuid: List<Pair</* guid */ String, ContentValues>>,
        toBeDeletedSessionGuids: List</* sessionGuid */ String>
    ) = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            contentValuesByGuid.forEach { (guid, contentValues) ->
                upsertSession(guid, contentValues)
            }
            toBeDeletedSessionGuids.forEach { toBeDeletedSessionGuid ->
                deleteSession(toBeDeletedSessionGuid)
            }
        }
    }

    /**
     * Updates a session with the given [contentValues]. A row is matched by its [guid].
     * If no row was affected by the update operation then an insert operation is performed
     * assuming that the session does not exist in the table.
     *
     * The [guid] column is used for matching an existing row because this column holds unique values
     * and to avoid an unneeded database SELECT just to retrieve the primary key of a row.
     *
     * This function must be called in the context of a [transaction] block.
     */
    private fun SQLiteDatabase.upsertSession(guid: String, contentValues: ContentValues) {
        val affectedRowsCount = updateRow(
            tableName = SessionsTable.NAME,
            contentValues = contentValues,
            columnName = GUID,
            columnValue = guid
        )
        if (affectedRowsCount == 0) {
            insert(
                tableName = SessionsTable.NAME,
                values = contentValues
            )
        }
    }

    /**
     * Delete the session identified by the given [sessionGuid] from the table.
     */
    private fun SQLiteDatabase.deleteSession(sessionGuid: String) = delete(
        tableName = SessionsTable.NAME,
        columnName = GUID,
        columnValue = sessionGuid
    )

    override fun querySessionByGuid(guid: String): Session {
        return try {
            query {
                read(SessionsTable.NAME,
                    // The value of "guid" is replaced with the value of the "_id"
                    // column when the session is queried initially to guarantee a unique value.
                    selection = "$GUID=?",
                    selectionArgs = arrayOf(guid))
            }.first()
        } catch (e: NoSuchElementException) {
            logging.report(LOG_TAG, "Sessions table does not contain a session with ID '$guid'. ${e.message}")
            throw e
        }
    }

    override fun querySessionsForDayIndexOrderedByDateUtc(dayIndex: Int) = query {
        read(SessionsTable.NAME,
            selection = "$DAY=?",
            selectionArgs = arrayOf(String.format("%d", dayIndex)),
            orderBy = DATE_UTC)
    }

    override fun querySessionsOrderedByDateUtc() = query {
        read(SessionsTable.NAME, orderBy = DATE_UTC)
    }

    override fun querySessionsWithoutRoom(roomName: String) = query {
        read(SessionsTable.NAME,
                selection = "$ROOM_NAME!=?",
                selectionArgs = arrayOf(roomName),
                orderBy = DATE_UTC
        )
    }

    override fun querySessionsWithinRoom(roomName: String) = query {
        read(SessionsTable.NAME,
                selection = "$ROOM_NAME=?",
                selectionArgs = arrayOf(roomName),
                orderBy = DATE_UTC
        )
    }

    private fun query(query: SQLiteDatabase.() -> Cursor): List<Session> = with(sqLiteOpenHelper.readableDatabase) {
        val cursor = try {
            query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return emptyList()
        }

        return cursor.map {
            val recordingOptOut =
                if (cursor.getInt(REC_OPTOUT) == REC_OPT_OUT_OFF)
                    Session.RECORDING_OPT_OUT_OFF
                else
                    Session.RECORDING_OPT_OUT_ON

            Session(
                    guid = cursor.getString(GUID),
                    abstractt = cursor.getString(ABSTRACT),
                    dateText = cursor.getString(DATE_TEXT),
                    dateUTC = cursor.getLong(DATE_UTC),
                    dayIndex = cursor.getInt(DAY),
                    description = cursor.getString(DESCR),
                    duration = cursor.getInt(DURATION),
                    feedbackUrl = cursor.getStringOrNull(FEEDBACK_URL),
                    language = cursor.getString(LANG),
                    links = cursor.getString(LINKS),
                    recordingLicense = cursor.getString(REC_LICENSE),
                    relativeStartTime = cursor.getInt(REL_START),
                    roomName = cursor.getString(ROOM_NAME),
                    roomIdentifier = cursor.getString(ROOM_IDENTIFIER),
                    roomIndex = cursor.getInt(ROOM_INDEX),
                    slug = cursor.getString(SLUG),
                    speakers = cursor.getString(SPEAKERS),
                    subtitle = cursor.getString(SUBTITLE),
                    startTime = cursor.getInt(START),
                    timeZoneOffset = cursor.getIntOrNull(TIME_ZONE_OFFSET),
                    title = cursor.getString(TITLE),
                    track = cursor.getString(TRACK),
                    type = cursor.getString(TYPE),
                    url = cursor.getString(URL),
                    recordingOptOut = recordingOptOut,
                    changedDayIndex = cursor.getInt(CHANGED_DAY_INDEX).isChanged,
                    changedDuration = cursor.getInt(CHANGED_DURATION).isChanged,
                    changedIsCanceled = cursor.getInt(CHANGED_IS_CANCELED).isChanged,
                    changedIsNew = cursor.getInt(CHANGED_IS_NEW).isChanged,
                    changedLanguage = cursor.getInt(CHANGED_LANGUAGE).isChanged,
                    changedRecordingOptOut = cursor.getInt(CHANGED_RECORDING_OPTOUT).isChanged,
                    changedRoomName = cursor.getInt(CHANGED_ROOM_NAME).isChanged,
                    changedSpeakers = cursor.getInt(CHANGED_SPEAKERS).isChanged,
                    changedStartTime = cursor.getInt(CHANGED_START_TIME).isChanged,
                    changedSubtitle = cursor.getInt(CHANGED_SUBTITLE).isChanged,
                    changedTitle = cursor.getInt(CHANGED_TITLE).isChanged,
                    changedTrack = cursor.getInt(CHANGED_TRACK).isChanged
            )
        }
    }

    override fun queryScheduleStatistic(): List<ColumnStatistic> =
        with(sqLiteOpenHelper.readableDatabase) {
            val cursor = try {
                read(StatisticsView.NAME)
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return emptyList()
            }

            val stats = cursor.use {
                if (cursor.moveToFirst()) {
                    listOf(
                        cursor.toColumnStatistic(
                            name = TITLE,
                            columnNameNone = TITLE_NONE,
                            columnNamePresent = TITLE_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = SUBTITLE,
                            columnNameNone = SUBTITLE_NONE,
                            columnNamePresent = SUBTITLE_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = DAY,
                            columnNameNone = DAY_INDEX_NONE,
                            columnNamePresent = DAY_INDEX_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = ROOM_NAME,
                            columnNameNone = ROOM_NAME_NONE,
                            columnNamePresent = ROOM_NAME_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = START,
                            columnNameNone = START_TIME_NONE,
                            columnNamePresent = START_TIME_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = DURATION,
                            columnNameNone = DURATION_NONE,
                            columnNamePresent = DURATION_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = SPEAKERS,
                            columnNameNone = SPEAKERS_NONE,
                            columnNamePresent = SPEAKERS_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = TRACK,
                            columnNameNone = TRACK_NONE,
                            columnNamePresent = TRACK_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = TYPE,
                            columnNameNone = TYPE_NONE,
                            columnNamePresent = TYPE_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = LANG,
                            columnNameNone = LANGUAGES_NONE,
                            columnNamePresent = LANGUAGES_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = ABSTRACT,
                            columnNameNone = ABSTRACT_NONE,
                            columnNamePresent = ABSTRACT_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = DESCR,
                            columnNameNone = DESCRIPTION_NONE,
                            columnNamePresent = DESCRIPTION_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = REL_START,
                            columnNameNone = RELATIVE_START_TIME_NONE,
                            columnNamePresent = RELATIVE_START_TIME_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = DATE_TEXT,
                            columnNameNone = DATE_TEXT_NONE,
                            columnNamePresent = DATE_TEXT_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = LINKS,
                            columnNameNone = LINKS_NONE,
                            columnNamePresent = LINKS_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = DATE_UTC,
                            columnNameNone = DATE_UTC_NONE,
                            columnNamePresent = DATE_UTC_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = ROOM_INDEX,
                            columnNameNone = ROOM_INDEX_NONE,
                            columnNamePresent = ROOM_INDEX_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = REC_LICENSE,
                            columnNameNone = RECORDING_LICENSE_NONE,
                            columnNamePresent = RECORDING_LICENSE_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = REC_OPTOUT,
                            columnNameNone = RECORDING_OPTOUT_NONE,
                            columnNamePresent = RECORDING_OPTOUT_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = SLUG,
                            columnNameNone = SLUG_NONE,
                            columnNamePresent = SLUG_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = URL,
                            columnNameNone = URL_NONE,
                            columnNamePresent = URL_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = TIME_ZONE_OFFSET,
                            columnNameNone = TIME_ZONE_OFFSET_NONE,
                            columnNamePresent = TIME_ZONE_OFFSET_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = ROOM_IDENTIFIER,
                            columnNameNone = ROOM_IDENTIFIER_NONE,
                            columnNamePresent = ROOM_IDENTIFIER_PRESENT,
                        ),
                        cursor.toColumnStatistic(
                            name = FEEDBACK_URL,
                            columnNameNone = FEEDBACK_URL_NONE,
                            columnNamePresent = FEEDBACK_URL_PRESENT,
                        ),
                    )
                } else {
                    emptyList()
                }
            }
            return stats
        }

    private fun Cursor.toColumnStatistic(
        name: String,
        columnNameNone: String,
        columnNamePresent: String,
    ) = ColumnStatistic(
        name = name,
        countNone = getInt(columnNameNone),
        countPresent = getInt(columnNamePresent),
    )

    private val Int.isChanged
        get() = this != 0

}
