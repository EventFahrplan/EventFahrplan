package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import info.metadude.android.eventfahrplan.database.models.Highlight

interface HighlightsDatabaseRepository {

    fun update(values: ContentValues, sessionId: String): Long
    fun query(): List<Highlight>
    fun queryBySessionId(sessionId: Int): Highlight?
    fun delete(sessionId: String): Int
    fun deleteAll(): Int

}
