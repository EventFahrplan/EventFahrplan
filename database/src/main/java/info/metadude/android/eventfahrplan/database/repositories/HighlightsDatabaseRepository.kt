package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper

interface HighlightsDatabaseRepository {

    companion object {
        fun get(context: Context): HighlightsDatabaseRepository =
            RealHighlightsDatabaseRepository(HighlightDBOpenHelper(context))
    }

    fun update(values: ContentValues, guid: String): Long
    fun query(): List<Highlight>
    fun queryByGuid(guid: String): Highlight?
    fun delete(guid: String): Int
    fun deleteAll(): Int

}
