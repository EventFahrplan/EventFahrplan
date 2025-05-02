@file:JvmName("SQLiteDatabaseExtensions")

package info.metadude.android.eventfahrplan.database.extensions

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction

internal fun SQLiteDatabase.insert(tableName: String, values: ContentValues): Long =
        insert(tableName, null, values)

internal fun SQLiteDatabase.read(
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

internal fun SQLiteDatabase.updateRow(
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

internal fun SQLiteDatabase.updateRows(
        tableName: String,
        contentValues: ContentValues,
        columnName: String,
        columnValues: Set<String>
): Int = updateRows(
        tableName = tableName,
        contentValues = contentValues,
        whereClause = if (columnValues.isEmpty()) null else "$columnName IN (${columnValues.joinToString { "?" }})",
        whereArgs = columnValues.toTypedArray()
)

internal fun SQLiteDatabase.updateRows(
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

internal fun SQLiteDatabase.delete(tableName: String, columnName: String? = null, columnValue: String? = null): Int {
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
internal fun SQLiteDatabase.columnExists(tableName: String, columnName: String): Boolean {
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

/**
 * Deletes the given [table][tableName] if it exists.
 */
internal fun SQLiteDatabase.dropTableIfExist(tableName: String) {
        execSQL("DROP TABLE IF EXISTS $tableName")
}

/**
 * Adds an INTEGER [column][columnName] to the given [table][tableName] with a [default value][default].
 */
internal fun SQLiteDatabase.addIntegerColumn(tableName: String, columnName: String, default: Int?) {
        val defaultValue = default?.toString() ?: "NULL"
        execSQL("ALTER TABLE $tableName ADD COLUMN $columnName INTEGER DEFAULT $defaultValue")
}

/**
 * Adds a TEXT [column][columnName] to the given [table][tableName] with a [default value][default].
 */
internal fun SQLiteDatabase.addTextColumn(tableName: String, columnName: String, default: String?) {
        val defaultValue = default?.let { "'$it'" } ?: "NULL"
        execSQL("ALTER TABLE $tableName ADD COLUMN $columnName TEXT DEFAULT $defaultValue")
}
