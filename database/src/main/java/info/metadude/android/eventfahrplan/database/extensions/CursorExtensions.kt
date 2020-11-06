// Source: https://github.com/android/android-ktx/blob/a6df8d11e35d4ffb6ce740767ea1258f14c8e036/src/main/java/androidx/core/database/Cursor.kt

// Aliases to other public API.
@file:Suppress("NOTHING_TO_INLINE")

package info.metadude.android.eventfahrplan.database.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

/**
 * Returns the value of the requested column as an integer.
 *
 * The result and whether this method throws an exception when the column value is null or the
 * column type is not an integral type is implementation-defined.
 *
 * @see Cursor.getColumnIndexOrThrow
 * @see Cursor.getInt
 */
inline fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndexOrThrow(columnName))

/**
 * Returns the value of the requested column as a long.
 *
 * The result and whether this method throws an exception when the column value is null or the
 * column type is not an integral type is implementation-defined.
 *
 * @see Cursor.getColumnIndexOrThrow
 * @see Cursor.getLong
 */
inline fun Cursor.getLong(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))

/**
 * Returns the value of the requested column as a string.
 *
 * The result and whether this method throws an exception when the column value is null or the
 * column type is not a string type is implementation-defined.
 *
 * @see Cursor.getColumnIndexOrThrow
 * @see Cursor.getString
 */
inline fun Cursor.getString(columnName: String): String = getString(getColumnIndexOrThrow(columnName))

/**
 * Returns the value of the requested column as a string or null.
 *
 * The result and whether this method throws an exception when the column type is not a string type
 * is implementation-defined.
 *
 * @see Cursor.getColumnIndexOrThrow
 * @see Cursor.getStringOrNull
 */
inline fun Cursor.getStringOrNull(columnName: String): String? = getStringOrNull(getColumnIndexOrThrow(columnName))

/**
 * Returns a list containing the results of applying the given [transform] function to each row
 * in the [Cursor]. Closes the Cursor afterwards.
 */
inline fun <T> Cursor.map(transform: (Cursor) -> T): List<T> = this.use {
    List(count) { index ->
        moveToPosition(index)
        transform(this)
    }
}
