package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.*
import info.metadude.android.eventfahrplan.database.extensions.*
import info.metadude.android.eventfahrplan.database.models.Alarm
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper

class AlarmsDatabaseRepository(

        private val sqLiteOpenHelper: AlarmsDBOpenHelper

) {

    fun insert(values: ContentValues, eventId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(AlarmsTable.NAME, EVENT_ID, eventId)
        }, {
            insert(AlarmsTable.NAME, values)
        })
    }

    fun query(): List<Alarm> = query {
        read(AlarmsTable.NAME)
    }

    fun query(eventId: String): List<Alarm> = query {
        read(AlarmsTable.NAME, selection = "$EVENT_ID=?", selectionArgs = arrayOf(eventId))
    }

    private fun query(query: SQLiteDatabase.() -> Cursor): List<Alarm> {
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Log.e(javaClass.name, "Failure on alarm query.")
            return emptyList()
        }

        val alarms = cursor.map {
            Alarm(
                    id = cursor.getInt(ID),
                    day = cursor.getInt(DAY),
                    eventId = cursor.getString(EVENT_ID),
                    time = cursor.getLong(TIME),
                    title = cursor.getString(EVENT_TITLE)
            )
        }

        if (alarms.isEmpty()) {
            Log.d(javaClass.name, "No alarms found.")
        }

        return alarms
    }

    fun deleteForAlarmId(alarmId: Int) = delete {
        delete(AlarmsTable.NAME, ID, "$alarmId")
    }

    fun deleteForEventId(eventId: String) = delete {
        delete(AlarmsTable.NAME, EVENT_ID, eventId)
    }

    private fun delete(query: SQLiteDatabase.() -> Int) =
            with(sqLiteOpenHelper) {
                writableDatabase.delete(query)
            }

}
