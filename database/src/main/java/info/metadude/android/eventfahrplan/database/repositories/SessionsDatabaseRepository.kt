package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns.*
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.extensions.*
import info.metadude.android.eventfahrplan.database.extensions.transaction
import info.metadude.android.eventfahrplan.database.models.Session
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper

class SessionsDatabaseRepository(

        private val sqLiteOpenHelper: SessionsDBOpenHelper,
        private val logging: Logging

) {

    fun insert(list: List<ContentValues>) = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            delete(SessionsTable.NAME)
            list.forEach { contentValues ->
                insert(SessionsTable.NAME, contentValues)
            }
        }
    }

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
