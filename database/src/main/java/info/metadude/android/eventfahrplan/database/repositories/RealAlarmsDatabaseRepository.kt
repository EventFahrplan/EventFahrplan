package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.commons.logging.Logging
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

internal class RealAlarmsDatabaseRepository(

        private val sqLiteOpenHelper: AlarmsDBOpenHelper,
        private val logging: Logging

) : AlarmsDatabaseRepository {

    private companion object {
        const val LOG_TAG = "AlarmsDatabaseRepository"
    }

    override fun update(values: ContentValues, sessionId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(AlarmsTable.NAME, SESSION_ID, sessionId)
        }, {
            insert(AlarmsTable.NAME, values)
        })
    }

    override fun query(): List<Alarm> = query {
        read(AlarmsTable.NAME, orderBy = TIME)
    }

    override fun query(sessionId: String): List<Alarm> = query {
        read(AlarmsTable.NAME, selection = "$SESSION_ID=?", selectionArgs = arrayOf(sessionId))
    }

    override fun query(query: SQLiteDatabase.() -> Cursor): List<Alarm> {
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.query()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            logging.e(LOG_TAG, "Failure on alarm query.")
            return emptyList()
        }

        val alarms = cursor.map {
            Alarm(
                id = cursor.getInt(ID),
                day = cursor.getInt(DAY),
                sessionId = cursor.getString(SESSION_ID),
                time = cursor.getLong(TIME),
                title = cursor.getString(SESSION_TITLE),
            )
        }

        if (alarms.isEmpty()) {
            logging.d(LOG_TAG, "No alarms found.")
        }

        return alarms
    }

    override fun deleteAll() = delete {
        delete(AlarmsTable.NAME)
    }

    override fun deleteForAlarmId(alarmId: Int) = delete {
        delete(AlarmsTable.NAME, ID, "$alarmId")
    }

    override fun deleteForSessionId(sessionId: String) = delete {
        delete(AlarmsTable.NAME, SESSION_ID, sessionId)
    }

    override fun delete(query: SQLiteDatabase.() -> Int) =
            with(sqLiteOpenHelper) {
                writableDatabase.delete(query)
            }

}
