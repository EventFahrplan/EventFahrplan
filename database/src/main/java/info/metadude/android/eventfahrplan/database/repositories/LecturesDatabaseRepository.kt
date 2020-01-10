package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns.*
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.extensions.*
import info.metadude.android.eventfahrplan.database.extensions.transaction
import info.metadude.android.eventfahrplan.database.models.Lecture
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper

class LecturesDatabaseRepository(

        private val sqLiteOpenHelper: LecturesDBOpenHelper,
        private val logging: Logging

) {

    fun insert(list: List<ContentValues>) = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            delete(LecturesTable.NAME)
            list.forEach { contentValues ->
                insert(LecturesTable.NAME, contentValues)
            }
        }
    }

    fun queryLectureByLectureId(lectureId: String): Lecture {
        return try {
            query {
                read(LecturesTable.NAME,
                        selection = "$EVENT_ID=?",
                        selectionArgs = arrayOf(lectureId))
            }.first()
        } catch (e: NoSuchElementException) {
            logging.report(javaClass.name, "Lectures table does not contain a lecture with ID $lectureId. ${e.message}")
            throw e
        }
    }

    fun queryLecturesForDayIndexOrderedByDateUtc(dayIndex: Int) = query {
        read(LecturesTable.NAME,
                selection = "$DAY=?",
                selectionArgs = arrayOf(String.format("%d", dayIndex)),
                orderBy = DATE_UTC)
    }

    fun queryLecturesOrderedByDateUtc() = query {
        read(LecturesTable.NAME, orderBy = DATE_UTC)
    }

    fun queryLecturesWithoutRoom(roomName: String) = query {
        read(LecturesTable.NAME,
                selection = "$ROOM!=?",
                selectionArgs = arrayOf(roomName),
                orderBy = DATE_UTC
        )
    }

    fun queryLecturesWithinRoom(roomName: String) = query {
        read(LecturesTable.NAME,
                selection = "$ROOM=?",
                selectionArgs = arrayOf(roomName),
                orderBy = DATE_UTC
        )
    }

    private fun query(query: SQLiteDatabase.() -> Cursor): List<Lecture> = with(sqLiteOpenHelper.readableDatabase) {
        val cursor = try {
            query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return emptyList()
        }

        return cursor.map {
            val recordingOptOut =
                    if (cursor.getInt(REC_OPTOUT) == REC_OPT_OUT_OFF)
                        Lecture.RECORDING_OPT_OUT_OFF
                    else
                        Lecture.RECORDING_OPT_OUT_ON

            Lecture(
                    eventId = cursor.getString(EVENT_ID),
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
