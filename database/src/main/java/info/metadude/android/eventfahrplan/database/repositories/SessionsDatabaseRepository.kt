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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_CANCELED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_IS_NEW
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_LANGUAGE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_RECORDING_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_ROOM
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SPEAKERS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TIME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.CHANGED_TRACK
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DATE_UTC
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DESCR
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.DURATION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LANG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.LINKS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_LICENSE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REC_OPTOUT
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.REL_START
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.ROOM_IDX
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
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getIntOrNull
import info.metadude.android.eventfahrplan.database.extensions.getLong
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.map
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.updateRow
import info.metadude.android.eventfahrplan.database.models.Session
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper

class SessionsDatabaseRepository(

        private val sqLiteOpenHelper: SessionsDBOpenHelper,
        private val logging: Logging

) {

    /**
     * Inserts the session ID into the [SessionByNotificationIdTable] and returns
     * the newly generated notification ID which is associated with the session ID.
     *
     * Notification IDs are incremented automatically.
     *
     * [sessionIdContentValues] is expected to be composed from
     * [SessionByNotificationIdTable.Columns.SESSION_ID] and the session ID value.
     *
     * See also: [android.database.sqlite.SQLiteDatabase.insert]
     */
    fun insertSessionId(sessionIdContentValues: ContentValues) = with(sqLiteOpenHelper) {
        writableDatabase.insert(SessionByNotificationIdTable.NAME, sessionIdContentValues)
    }.toInt()

    /**
     * Deletes the [SessionByNotificationIdTable] row associated with the given unique [notificationId].
     * Returns the number of affected rows.
     */
    fun deleteSessionIdByNotificationId(notificationId: Int) = with(sqLiteOpenHelper) {
        writableDatabase.delete(SessionByNotificationIdTable.NAME, SessionByNotificationIdTable.Columns._ID, "$notificationId")
    }


    /**
     * Updates or inserts sessions based on the given [contentValuesBySessionId].
     * Removes all sessions identified by their [session IDs][toBeDeletedSessionIds].
     */
    fun updateSessions(
            contentValuesBySessionId: List<Pair</* sessionId */ String, ContentValues>>,
            toBeDeletedSessionIds: List</* sessionId */ String>
    ) = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            contentValuesBySessionId.forEach { (sessionId, contentValues) ->
                upsertSession(sessionId, contentValues)
            }
            toBeDeletedSessionIds.forEach { toBeDeletedSessionId ->
                deleteSession(toBeDeletedSessionId)
            }
        }
    }

    /**
     * Updates a session with the given [contentValues]. A row is matched by its [sessionId].
     * If no row was affected by the update operation then an insert operation is performed
     * assuming that the session does not exist in the table.
     *
     * This function must be called in the context of a [transaction] block.
     */
    private fun SQLiteDatabase.upsertSession(sessionId: String, contentValues: ContentValues) {
        val affectedRowsCount = updateRow(
                tableName = SessionsTable.NAME,
                contentValues = contentValues,
                columnName = SESSION_ID,
                columnValue = sessionId
        )
        if (affectedRowsCount == 0) {
            insert(
                    tableName = SessionsTable.NAME,
                    values = contentValues
            )
        }
    }

    /**
     * Delete the session identified by the given [sessionId] from the table.
     */
    private fun SQLiteDatabase.deleteSession(sessionId: String) = delete(
            tableName = SessionsTable.NAME,
            columnName = SESSION_ID,
            columnValue = sessionId
    )

    fun querySessionBySessionId(sessionId: String): Session {
        return try {
            query {
                read(SessionsTable.NAME,
                        selection = "$SESSION_ID=?",
                        selectionArgs = arrayOf(sessionId))
            }.first()
        } catch (e: NoSuchElementException) {
            logging.report(javaClass.simpleName, "Sessions table does not contain a session with ID '$sessionId'. ${e.message}")
            throw e
        }
    }

    fun querySessionsForDayIndexOrderedByDateUtc(dayIndex: Int) = query {
        read(SessionsTable.NAME,
                selection = "$DAY=?",
                selectionArgs = arrayOf(String.format("%d", dayIndex)),
                orderBy = DATE_UTC)
    }

    fun querySessionsOrderedByDateUtc() = query {
        read(SessionsTable.NAME, orderBy = DATE_UTC)
    }

    fun querySessionsWithoutRoom(roomName: String) = query {
        read(SessionsTable.NAME,
                selection = "$ROOM!=?",
                selectionArgs = arrayOf(roomName),
                orderBy = DATE_UTC
        )
    }

    fun querySessionsWithinRoom(roomName: String) = query {
        read(SessionsTable.NAME,
                selection = "$ROOM=?",
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
                    sessionId = cursor.getString(SESSION_ID),
                    abstractt = cursor.getString(ABSTRACT),
                    date = cursor.getString(DATE),
                    dateUTC = cursor.getLong(DATE_UTC),
                    dayIndex = cursor.getInt(DAY),
                    description = cursor.getString(DESCR),
                    duration = cursor.getInt(DURATION),
                    language = cursor.getString(LANG),
                    links = cursor.getString(LINKS),
                    recordingLicense = cursor.getString(REC_LICENSE),
                    relativeStartTime = cursor.getInt(REL_START),
                    room = cursor.getString(ROOM),
                    roomIndex = cursor.getInt(ROOM_IDX),
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
                    changedDay = cursor.getInt(CHANGED_DAY).isChanged,
                    changedDuration = cursor.getInt(CHANGED_DURATION).isChanged,
                    changedIsCanceled = cursor.getInt(CHANGED_IS_CANCELED).isChanged,
                    changedIsNew = cursor.getInt(CHANGED_IS_NEW).isChanged,
                    changedLanguage = cursor.getInt(CHANGED_LANGUAGE).isChanged,
                    changedRecordingOptOut = cursor.getInt(CHANGED_RECORDING_OPTOUT).isChanged,
                    changedRoom = cursor.getInt(CHANGED_ROOM).isChanged,
                    changedSpeakers = cursor.getInt(CHANGED_SPEAKERS).isChanged,
                    changedSubtitle = cursor.getInt(CHANGED_SUBTITLE).isChanged,
                    changedTime = cursor.getInt(CHANGED_TIME).isChanged,
                    changedTitle = cursor.getInt(CHANGED_TITLE).isChanged,
                    changedTrack = cursor.getInt(CHANGED_TRACK).isChanged
            )
        }
    }

    private val Int.isChanged
        get() = this != 0

}
