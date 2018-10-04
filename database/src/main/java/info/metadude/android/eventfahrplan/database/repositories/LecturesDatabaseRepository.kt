package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns.*
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values.REC_OPT_OUT_OFF
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.models.Lecture
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper

class LecturesDatabaseRepository(

        private val sqLiteOpenHelper: LecturesDBOpenHelper

) {

    fun insert(list: List<ContentValues>) = with(sqLiteOpenHelper.writableDatabase) {
        try {
            beginTransaction()
            delete(LecturesTable.NAME)
            list.forEach {
                insert(LecturesTable.NAME, it)
            }
            setTransactionSuccessful()
        } catch (ignore: SQLException) {
            // Fail silently
        } finally {
            endTransaction()
            close()
            sqLiteOpenHelper.close()
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
            var lecture = Lecture(eventId = cursor.getString(cursor.getColumnIndex(EVENT_ID)))
            lecture = lecture.copy(abstractt = cursor.getString(cursor.getColumnIndex(ABSTRACT)))
            lecture = lecture.copy(date = cursor.getString(cursor.getColumnIndex(DATE)))
            lecture = lecture.copy(dateUTC = cursor.getLong(cursor.getColumnIndex(DATE_UTC)))
            lecture = lecture.copy(dayIndex = cursor.getInt(cursor.getColumnIndex(DAY)))
            lecture = lecture.copy(description = cursor.getString(cursor.getColumnIndex(DESCR)))
            lecture = lecture.copy(duration = cursor.getInt(cursor.getColumnIndex(DURATION)))
            lecture = lecture.copy(language = cursor.getString(cursor.getColumnIndex(LANG)))
            lecture = lecture.copy(links = cursor.getString(cursor.getColumnIndex(LINKS)))
            lecture = lecture.copy(recordingLicense = cursor.getString(cursor.getColumnIndex(REC_LICENSE)))
            lecture = lecture.copy(relativeStartTime = cursor.getInt(cursor.getColumnIndex(REL_START)))
            lecture = lecture.copy(room = cursor.getString(cursor.getColumnIndex(ROOM)))
            lecture = lecture.copy(roomIndex = cursor.getInt(cursor.getColumnIndex(ROOM_IDX)))
            lecture = lecture.copy(slug = cursor.getString(cursor.getColumnIndex(SLUG)))
            lecture = lecture.copy(speakers = cursor.getString(cursor.getColumnIndex(SPEAKERS)))
            lecture = lecture.copy(subtitle = cursor.getString(cursor.getColumnIndex(SUBTITLE)))
            lecture = lecture.copy(startTime = cursor.getInt(cursor.getColumnIndex(START)))
            lecture = lecture.copy(title = cursor.getString(cursor.getColumnIndex(TITLE)))
            lecture = lecture.copy(track = cursor.getString(cursor.getColumnIndex(TRACK)))
            lecture = lecture.copy(type = cursor.getString(cursor.getColumnIndex(TYPE)))
            lecture = lecture.copy(url = cursor.getString(cursor.getColumnIndex(URL)))
            val recordingOptOut =
                    if (cursor.getInt(cursor.getColumnIndex(REC_OPTOUT)) == REC_OPT_OUT_OFF)
                        Lecture.RECORDING_OPT_OUT_OFF
                    else
                        Lecture.RECORDING_OPT_OUT_ON
            lecture = lecture.copy(recordingOptOut = recordingOptOut)

            lecture = lecture.copy(changedDay = cursor.getInt(cursor.getColumnIndex(CHANGED_DAY)).isChanged)
            lecture = lecture.copy(changedDuration = cursor.getInt(cursor.getColumnIndex(CHANGED_DURATION)).isChanged)
            lecture = lecture.copy(changedIsCanceled = cursor.getInt(cursor.getColumnIndex(CHANGED_IS_CANCELED)).isChanged)
            lecture = lecture.copy(changedIsNew = cursor.getInt(cursor.getColumnIndex(CHANGED_IS_NEW)).isChanged)
            lecture = lecture.copy(changedLanguage = cursor.getInt(cursor.getColumnIndex(CHANGED_LANGUAGE)).isChanged)
            lecture = lecture.copy(changedRecordingOptOut = cursor.getInt(cursor.getColumnIndex(CHANGED_RECORDING_OPTOUT)).isChanged)
            lecture = lecture.copy(changedRoom = cursor.getInt(cursor.getColumnIndex(CHANGED_ROOM)).isChanged)
            lecture = lecture.copy(changedSpeakers = cursor.getInt(cursor.getColumnIndex(CHANGED_SPEAKERS)).isChanged)
            lecture = lecture.copy(changedSubtitle = cursor.getInt(cursor.getColumnIndex(CHANGED_SUBTITLE)).isChanged)
            lecture = lecture.copy(changedTime = cursor.getInt(cursor.getColumnIndex(CHANGED_TIME)).isChanged)
            lecture = lecture.copy(changedTitle = cursor.getInt(cursor.getColumnIndex(CHANGED_TITLE)).isChanged)
            lecture = lecture.copy(changedTrack = cursor.getInt(cursor.getColumnIndex(CHANGED_TRACK)).isChanged)
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