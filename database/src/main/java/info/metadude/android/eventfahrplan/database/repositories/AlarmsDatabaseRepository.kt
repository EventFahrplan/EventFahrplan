package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.database.models.Alarm
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper

interface AlarmsDatabaseRepository {

    companion object {
        fun get(context: Context, logging: Logging): AlarmsDatabaseRepository =
            RealAlarmsDatabaseRepository(AlarmsDBOpenHelper(context), logging)
    }

    fun update(values: ContentValues, sessionId: String): Long
    fun query(): List<Alarm>
    fun query(sessionId: String): List<Alarm>
    fun query(query: SQLiteDatabase.() -> Cursor): List<Alarm>
    fun deleteAll(): Int
    fun deleteForAlarmId(alarmId: Int): Int
    fun deleteForSessionId(sessionId: String): Int
    fun delete(query: SQLiteDatabase.() -> Int): Int

}
