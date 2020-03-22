package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun SQLiteDatabase.insert(tableName: String, values: ContentValues): Long =
        insert(tableName, null, values)

fun SQLiteDatabase.read(
        tableName: String,
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: String? = null

): Cursor = query(
        tableName,
        columns,
        selection,
        selectionArgs,
        groupBy,
        having,
        orderBy,
        limit
)

fun SQLiteDatabase.updateRows(
        tableName: String,
        contentValues: ContentValues,
        whereClause: String? = null,
        whereArgs: Array<String>? = null
): Int = update(
        tableName,
        contentValues,
        whereClause,
        whereArgs
)

fun SQLiteDatabase.delete(tableName: String, columnName: String? = null, columnValue: String? = null): Int {
    val whereClause = if (columnName == null) null else "$columnName=?"
    val whereArgs = if (columnValue == null) null else arrayOf(columnValue)
    return delete(tableName, whereClause, whereArgs)
}

/**
 * Executes the delete [query] within a transaction.
 */
internal fun SQLiteDatabase.delete(query: SQLiteDatabase.() -> Int) =
        transaction {
            query()
        }

/**
 * Executes the [delete] and the [insert] queries within a transaction.
 */
internal fun SQLiteDatabase.upsert(delete: SQLiteDatabase.() -> Int, insert: SQLiteDatabase.() -> Long) =
        transaction {
            delete()
            insert()
        }

/**
 * Runs [body] in a transaction marking it as successful if it completes without exception.
 *
 * @param exclusive Run in `EXCLUSIVE` mode when true, `IMMEDIATE` mode otherwise.
 *
 * Source: https://github.com/android/android-ktx/blob/a6df8d11e35d4ffb6ce740767ea1258f14c8e036/src/main/java/androidx/core/database/sqlite/SQLiteDatabase.kt
 */
internal inline fun <T> SQLiteDatabase.transaction(
        exclusive: Boolean = true,
        body: SQLiteDatabase.() -> T
): T {
    if (exclusive) {
        beginTransaction()
    } else {
        beginTransactionNonExclusive()
    }
    try {
        val result = body()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}
