package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.content.Context
import info.metadude.android.eventfahrplan.database.models.Favorite
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.FavoritesDBOpenHelper

interface FavoritesDatabaseRepository {

    companion object {
        fun get(context: Context): FavoritesDatabaseRepository =
            RealFavoritesDatabaseRepository(FavoritesDBOpenHelper(context))
    }

    fun update(values: ContentValues, sessionId: String): Long
    fun query(): List<Favorite>
    fun queryBySessionId(sessionId: String): Favorite?
    fun delete(sessionIds: Set<String>): Int
    fun deleteAll(): Int

}
