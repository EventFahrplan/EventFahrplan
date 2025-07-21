package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.getStringOrNull
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.upsert
import info.metadude.android.eventfahrplan.database.models.HttpHeader
import info.metadude.android.eventfahrplan.database.models.Meta
import info.metadude.android.eventfahrplan.database.models.ScheduleGenerator
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper

internal class RealMetaDatabaseRepository(

        private val sqLiteOpenHelper: MetaDBOpenHelper

) : MetaDatabaseRepository {

    override fun insert(values: ContentValues) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(MetasTable.NAME)
        }, {
            insert(MetasTable.NAME, values)
        })
    }

    override fun query(): Meta {
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.read(MetasTable.NAME)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return Meta()
        }

        val meta = cursor.use {
            if (cursor.moveToFirst()) {
                Meta(
                        numDays = cursor.getInt(NUM_DAYS),
                        version = cursor.getString(VERSION),
                        timeZoneName = cursor.getStringOrNull(TIME_ZONE_NAME),
                        title = cursor.getString(TITLE),
                        subtitle = cursor.getString(SUBTITLE),
                        httpHeader = cursor.getHttpHeader(),
                        scheduleGenerator = cursor.getScheduleGenerator(),
                )
            } else {
                Meta()
            }
        }

        return meta
    }

}

private fun Cursor.getHttpHeader() = HttpHeader(
    eTag = getString(SCHEDULE_ETAG),
    lastModified = getString(SCHEDULE_LAST_MODIFIED),
)

private fun Cursor.getScheduleGenerator() = ScheduleGenerator(
    name = getStringOrNull(SCHEDULE_GENERATOR_NAME),
    version = getStringOrNull(SCHEDULE_GENERATOR_VERSION),
)
