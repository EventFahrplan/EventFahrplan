package info.metadude.android.eventfahrplan.database.repositories

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.core.database.sqlite.transaction
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.IS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Columns.SESSION_ID
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Values.MARKED_AS_FAVORITE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.FavoritesTable.Values.NOT_MARKED_AS_FAVORITE
import info.metadude.android.eventfahrplan.database.extensions.delete
import info.metadude.android.eventfahrplan.database.extensions.getInt
import info.metadude.android.eventfahrplan.database.extensions.getString
import info.metadude.android.eventfahrplan.database.extensions.insert
import info.metadude.android.eventfahrplan.database.extensions.map
import info.metadude.android.eventfahrplan.database.extensions.read
import info.metadude.android.eventfahrplan.database.extensions.updateRows
import info.metadude.android.eventfahrplan.database.extensions.upsert
import info.metadude.android.eventfahrplan.database.models.Favorite
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.FavoritesDBOpenHelper

internal class RealFavoritesDatabaseRepository(

        private val sqLiteOpenHelper: FavoritesDBOpenHelper

) : FavoritesDatabaseRepository {

    override fun update(values: ContentValues, sessionId: String) = with(sqLiteOpenHelper) {
        writableDatabase.upsert({
            delete(FavoritesTable.NAME, SESSION_ID, sessionId)
        }, {
            insert(FavoritesTable.NAME, values)
        })
    }

    override fun query(): List<Favorite> {
        val favorites = mutableListOf<Favorite>()
        val database = sqLiteOpenHelper.readableDatabase

        val cursor = try {
            database.read(FavoritesTable.NAME, orderBy = SESSION_ID)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            return favorites.toList()
        }

        return cursor.map {
            val sessionId = cursor.getString(SESSION_ID)
            val favoriteState = cursor.getInt(IS_FAVORITE)
            val isFavorite = favoriteState == MARKED_AS_FAVORITE

            Favorite(sessionId, isFavorite)
        }
    }

    override fun queryBySessionId(sessionId: String): Favorite? {
        val database = sqLiteOpenHelper.readableDatabase
        val cursor = try {
            database.read(
                tableName = FavoritesTable.NAME,
                selection = "$SESSION_ID=?",
                selectionArgs = arrayOf(sessionId)
            )
        } catch (_: SQLiteException) {
            return null
        }

        return cursor.use {
            if (cursor.moveToFirst()) {
                val favoriteState = cursor.getInt(IS_FAVORITE)
                val isFavorite = favoriteState == MARKED_AS_FAVORITE
                Favorite(sessionId, isFavorite)
            } else {
                null
            }
        }
    }

    /**
     * Resets the value of the [IS_FAVORITE] column to [`false`][NOT_MARKED_AS_FAVORITE] for the rows
     * with the given [sessionIds].
     */
    override fun delete(sessionIds: Set<String>): Int {
        with(sqLiteOpenHelper) {
            return writableDatabase.updateRows(
                tableName = FavoritesTable.NAME,
                contentValues = ContentValues().apply {
                    put(IS_FAVORITE, NOT_MARKED_AS_FAVORITE)
                },
                columnName = SESSION_ID,
                columnValues = sessionIds,
            )
        }
    }

    /**
     * Resets the value of the [IS_FAVORITE] column to [`false`][NOT_MARKED_AS_FAVORITE] for each row.
     * Rows are not removed.
     */
    override fun deleteAll() = with(sqLiteOpenHelper) {
        writableDatabase.transaction {
            updateRows(
                    tableName = FavoritesTable.NAME,
                    contentValues = ContentValues().apply {
                        put(IS_FAVORITE, NOT_MARKED_AS_FAVORITE)
                    })
        }
    }

}
