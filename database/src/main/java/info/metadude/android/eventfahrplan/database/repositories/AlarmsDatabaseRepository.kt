package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.*
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.models.Alarm
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper

class AlarmsDatabaseRepository(

        private val sqLiteOpenHelper: AlarmsDBOpenHelper

) {

    fun insert(values: ContentValues, eventId: String) = with(sqLiteOpenHelper.writableDatabase) {
        try {
            beginTransaction()
            delete(AlarmsTable.NAME, EVENT_ID, eventId)
            insert(AlarmsTable.NAME, values)
            setTransactionSuccessful()
        } catch (ignore: SQLException) {
            // Fail silently
        } finally {
            endTransaction()
            close()
            sqLiteOpenHelper.close()
        }
    }

    fun query(): List<Alarm> = query {
        read(AlarmsTable.NAME)
    }

    fun query(eventId: String): List<Alarm> = query {
        read(AlarmsTable.NAME, selection = "$EVENT_ID=?", selectionArgs = arrayOf(eventId))
    }

    private fun query(query: SQLiteDatabase.() -> Cursor): List<Alarm> {
        val alarms = mutableListOf<Alarm>()
        val database = sqLiteOpenHelper.readableDatabase
        val cursor: Cursor

        try {
            cursor = database.query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Log.e(javaClass.name, "Failure on alarm query.")
            database.close()
            sqLiteOpenHelper.close()
            return alarms.toList()
        }

        if (cursor.count == 0) {
            cursor.close()
            database.close()
            sqLiteOpenHelper.close()
            Log.d(javaClass.name, "No alarms found.")
            return alarms.toList()
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            var alarm = Alarm()
            alarm = alarm.copy(id = cursor.getInt(cursor.getColumnIndex(ID)))
            alarm = alarm.copy(day = cursor.getInt(cursor.getColumnIndex(DAY)))
            alarm = alarm.copy(eventId = cursor.getString(cursor.getColumnIndex(EVENT_ID)))
            alarm = alarm.copy(time = cursor.getLong(cursor.getColumnIndex(TIME)))
            alarm = alarm.copy(title = cursor.getString(cursor.getColumnIndex(EVENT_TITLE)))
            alarms.add(alarm)
            cursor.moveToNext()
        }
        cursor.close()
        database.close()
        sqLiteOpenHelper.close()

        return alarms.toList()
    }

    fun deleteForAlarmId(alarmId: Int, closeSQLiteOpenHelper: Boolean) = delete(closeSQLiteOpenHelper) {
        delete(AlarmsTable.NAME, ID, "$alarmId")
    }

    fun deleteForEventId(eventId: String) = delete {
        delete(AlarmsTable.NAME, EVENT_ID, eventId)
    }

    private fun delete(closeSQLiteOpenHelper: Boolean = true, query: SQLiteDatabase.() -> Int) =
            with(sqLiteOpenHelper.writableDatabase) {
                try {
                    beginTransaction()
                    query()
                    setTransactionSuccessful()
                } catch (ignore: SQLException) {
                    // Fail silently
                } finally {
                    endTransaction()
                    close()
                    if (closeSQLiteOpenHelper) {
                        sqLiteOpenHelper.close()
                    }
                }
            }

}
