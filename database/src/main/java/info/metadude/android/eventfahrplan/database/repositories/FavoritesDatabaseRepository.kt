package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import info.metadude.android.eventfahrplan.database.models.Favorite
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper

interface HighlightsDatabaseRepository {

    companion object {
        fun get(context: Context): HighlightsDatabaseRepository =
            RealHighlightsDatabaseRepository(HighlightDBOpenHelper(context))
    }

    fun update(values: ContentValues, sessionId: String): Long
    fun query(): List<Favorite>
    fun queryBySessionId(sessionId: String): Favorite?
    fun delete(sessionIds: Set<String>): Int
    fun deleteAll(): Int

}
