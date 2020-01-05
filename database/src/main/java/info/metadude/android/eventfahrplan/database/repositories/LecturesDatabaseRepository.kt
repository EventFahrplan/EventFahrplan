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
import info.metadude.android.eventfahrplan.database.models.Lecture
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper

class LecturesDatabaseRepository(

        private val sqLiteOpenHelper: LecturesDBOpenHelper,
        private val logging: Logging

) {

    fun insert(list: List<ContentValues>) = with(sqLiteOpenHelper) {
        writableDatabase.use {
            it.transaction {
                delete(LecturesTable.NAME)
                list.forEach { contentValues ->
                    insert(LecturesTable.NAME, contentValues)
                }
            }
        }
        close()
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
        val lectures = mutableListOf<Lecture>()
        val cursor: Cursor

        try {
            cursor = query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            close()
            sqLiteOpenHelper.close()
            return lectures.toList()
        }

        if (cursor.count == 0) {
            cursor.close()
            close()
            sqLiteOpenHelper.close()
            return lectures.toList()
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            var lecture = Lecture(eventId = cursor.getString(EVENT_ID))
            lecture = lecture.copy(abstractt = cursor.getString(ABSTRACT))
            lecture = lecture.copy(date = cursor.getString(DATE))
            lecture = lecture.copy(dateUTC = cursor.getLong(DATE_UTC))
            lecture = lecture.copy(dayIndex = cursor.getInt(DAY))
            lecture = lecture.copy(description = cursor.getString(DESCR))
            lecture = lecture.copy(duration = cursor.getInt(DURATION))
            lecture = lecture.copy(language = cursor.getString(LANG))
            lecture = lecture.copy(links = cursor.getString(LINKS))
            lecture = lecture.copy(recordingLicense = cursor.getString(REC_LICENSE))
            lecture = lecture.copy(relativeStartTime = cursor.getInt(REL_START))
            lecture = lecture.copy(room = cursor.getString(ROOM))
            lecture = lecture.copy(roomIndex = cursor.getInt(ROOM_IDX))
            lecture = lecture.copy(slug = cursor.getString(SLUG))
            lecture = lecture.copy(speakers = cursor.getString(SPEAKERS))
            lecture = lecture.copy(subtitle = cursor.getString(SUBTITLE))
            lecture = lecture.copy(startTime = cursor.getInt(START))
            lecture = lecture.copy(title = cursor.getString(TITLE))
            lecture = lecture.copy(track = cursor.getString(TRACK))
            lecture = lecture.copy(type = cursor.getString(TYPE))
            lecture = lecture.copy(url = cursor.getString(URL))
            val recordingOptOut =
                    if (cursor.getInt(REC_OPTOUT) == REC_OPT_OUT_OFF)
                        Lecture.RECORDING_OPT_OUT_OFF
                    else
                        Lecture.RECORDING_OPT_OUT_ON
            lecture = lecture.copy(recordingOptOut = recordingOptOut)

            lecture = lecture.copy(changedDay = cursor.getInt(CHANGED_DAY).isChanged)
            lecture = lecture.copy(changedDuration = cursor.getInt(CHANGED_DURATION).isChanged)
            lecture = lecture.copy(changedIsCanceled = cursor.getInt(CHANGED_IS_CANCELED).isChanged)
            lecture = lecture.copy(changedIsNew = cursor.getInt(CHANGED_IS_NEW).isChanged)
            lecture = lecture.copy(changedLanguage = cursor.getInt(CHANGED_LANGUAGE).isChanged)
            lecture = lecture.copy(changedRecordingOptOut = cursor.getInt(CHANGED_RECORDING_OPTOUT).isChanged)
            lecture = lecture.copy(changedRoom = cursor.getInt(CHANGED_ROOM).isChanged)
            lecture = lecture.copy(changedSpeakers = cursor.getInt(CHANGED_SPEAKERS).isChanged)
            lecture = lecture.copy(changedSubtitle = cursor.getInt(CHANGED_SUBTITLE).isChanged)
            lecture = lecture.copy(changedTime = cursor.getInt(CHANGED_TIME).isChanged)
            lecture = lecture.copy(changedTitle = cursor.getInt(CHANGED_TITLE).isChanged)
            lecture = lecture.copy(changedTrack = cursor.getInt(CHANGED_TRACK).isChanged)
            lectures.add(lecture)
            cursor.moveToNext()
        }
        cursor.close()
        close()
        sqLiteOpenHelper.close()
        return lectures.toList()
    }

    private val Int.isChanged
        get() = this != 0

}
