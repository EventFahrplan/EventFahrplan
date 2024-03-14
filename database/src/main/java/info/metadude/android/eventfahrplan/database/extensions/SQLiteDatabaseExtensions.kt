@file:JvmName("SQLiteDatabaseExtensions")

package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction

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

fun SQLiteDatabase.updateRow(
        tableName: String,
        contentValues: ContentValues,
        columnName: String,
        columnValue: String
): Int = updateRows(
        tableName = tableName,
        contentValues = contentValues,
        whereClause = "$columnName=?",
        whereArgs = arrayOf(columnValue)
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
 * Returns `true` if the given [column][columnName] is present
 * in the [table][tableName], otherwise `false`.
 *
 * Throws an [IllegalArgumentException] if the schema does not contain
 * the expected column named `name`.
 */
fun SQLiteDatabase.columnExists(tableName: String, columnName: String): Boolean {
        val cursor = rawQuery("PRAGMA table_info($tableName)", null)
        while (cursor.moveToNext()) {
                // "name" is the name of the schema column which contains the table column names.
                val schemaColumnIndex = cursor.getColumnIndexOrThrow("name")
                if (schemaColumnIndex != -1) {
                        val currentColumnName = cursor.getString(schemaColumnIndex)
                        if (columnName == currentColumnName) {
                                cursor.close()
                                return true
                        }
                }
        }
        cursor.close()
        return false
}
