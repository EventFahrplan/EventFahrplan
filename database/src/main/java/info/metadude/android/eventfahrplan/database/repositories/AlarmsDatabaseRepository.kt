package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.DAY
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.SESSION_TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns.TIME
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getLong
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.map
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.upsert
import info.metadude.android.eventfahrplan.database.models.Alarm
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper

class AlarmsDatabaseRepository(

        private val sqLiteOpenHelper: AlarmsDBOpenHelper

) {

    fun update(values: ContentValues, sessionId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(AlarmsTable.NAME, SESSION_ID, sessionId)
        }, {
            insert(AlarmsTable.NAME, values)
        })
    }

    fun query(): List<Alarm> = query {
        read(AlarmsTable.NAME)
    }

    fun query(sessionId: String): List<Alarm> = query {
        read(AlarmsTable.NAME, selection = "$SESSION_ID=?", selectionArgs = arrayOf(sessionId))
    }

    private fun query(query: SQLiteDatabase.() -> Cursor): List<Alarm> {
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Log.e(javaClass.simpleName, "Failure on alarm query.")
            return emptyList()
        }

        val alarms = cursor.map {
            Alarm(
                    id = cursor.getInt(ID),
                    day = cursor.getInt(DAY),
                    sessionId = cursor.getString(SESSION_ID),
                    time = cursor.getLong(TIME),
                    title = cursor.getString(SESSION_TITLE)
            )
        }

        if (alarms.isEmpty()) {
            Log.d(javaClass.simpleName, "No alarms found.")
        }

        return alarms
    }

    fun deleteForAlarmId(alarmId: Int) = delete {
        delete(AlarmsTable.NAME, ID, "$alarmId")
    }

    fun deleteForSessionId(sessionId: String) = delete {
        delete(AlarmsTable.NAME, SESSION_ID, sessionId)
    }

    private fun delete(query: SQLiteDatabase.() -> Int) =
            with(sqLiteOpenHelper) {
                writableDatabase.delete(query)
            }

}
