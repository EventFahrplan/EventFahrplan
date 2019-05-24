package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.*
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.upsert
import info.metadude.android.eventfahrplan.database.models.Meta
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper

class MetaDatabaseRepository(

        private val sqLiteOpenHelper: MetaDBOpenHelper

) {

    fun insert(values: ContentValues) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(MetasTable.NAME)
        }, {
            insert(MetasTable.NAME, values)
        })
        close()
    }

    fun query(): Meta {
        var meta = Meta()
        val database = sqLiteOpenHelper.readableDatabase

        val cursor: Cursor
        try {
            cursor = database.read(MetasTable.NAME)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            database.close()
            sqLiteOpenHelper.close()
            return meta
        }

        if (cursor.count > 0) {
            cursor.moveToFirst()
            val columnIndexNumDays = cursor.getColumnIndex(NUM_DAYS)
            if (cursor.columnCount > columnIndexNumDays) {
                meta = meta.copy(numDays = cursor.getInt(columnIndexNumDays))
            }
            val columnIndexVersion = cursor.getColumnIndex(VERSION)
            if (cursor.columnCount > columnIndexVersion) {
                meta = meta.copy(version = cursor.getString(columnIndexVersion))
            }
            val columnIndexTitle = cursor.getColumnIndex(TITLE)
            if (cursor.columnCount > columnIndexTitle) {
                meta = meta.copy(title = cursor.getString(columnIndexTitle))
            }
            val columnIndexSubTitle = cursor.getColumnIndex(SUBTITLE)
            if (cursor.columnCount > columnIndexSubTitle) {
                meta = meta.copy(subtitle = cursor.getString(columnIndexSubTitle))
            }
            val columnIndexDayChangeHour = cursor.getColumnIndex(DAY_CHANGE_HOUR)
            if (cursor.columnCount > columnIndexDayChangeHour) {
                meta = meta.copy(dayChangeHour = cursor.getInt(columnIndexDayChangeHour))
            }
            val columnIndexDayChangeMinute = cursor.getColumnIndex(DAY_CHANGE_MINUTE)
            if (cursor.columnCount > columnIndexDayChangeMinute) {
                meta = meta.copy(dayChangeMinute = cursor.getInt(columnIndexDayChangeMinute))
            }
            val columnIndexEtag = cursor.getColumnIndex(ETAG)
            if (cursor.columnCount > columnIndexEtag) {
                meta = meta.copy(eTag = cursor.getString(columnIndexEtag))
            }
        }

        Log.d(javaClass.name, "query(): $meta")

        cursor.close()
        database.close()
        sqLiteOpenHelper.close()

        return meta
    }

}
