package info.metadude.android.eventfahrplan.database.extensions

import android.database.sqlite.SQLiteDatabase
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class SQLiteDatabaseExtensionsTest {

    private val db = mock<SQLiteDatabase>()

    @Nested
    inner class DropTable {
        @Test
        fun `dropTableIfExist with text`() {
            db.dropTableIfExist("sessions")
            verifyInvokedOnce(db).execSQL("DROP TABLE IF EXISTS sessions")
        }
    }

    @Nested
    inner class AddTextColumn {

        @Test
        fun `addTextColumn with NULL`() {
            db.addTextColumn(tableName = "sessions", columnName = "url", default = null)
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN url TEXT DEFAULT NULL")
        }

        @Test
        fun `addTextColumn with empty string`() {
            db.addTextColumn(tableName = "sessions", columnName = "url", default = "")
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN url TEXT DEFAULT ''")
        }

        @Test
        fun `addTextColumn with number`() {
            db.addTextColumn(tableName = "sessions", columnName = "url", default = "0")
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN url TEXT DEFAULT '0'")
        }

        @Test
        fun `addTextColumn with boolean word`() {
            db.addTextColumn(tableName = "sessions", columnName = "url", default = "true")
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN url TEXT DEFAULT 'true'")
        }

        @Test
        fun `addTextColumn with text`() {
            db.addTextColumn(tableName = "sessions", columnName = "url", default = "lorem")
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN url TEXT DEFAULT 'lorem'")
        }

    }

    @Nested
    inner class AddIntegerColumn {

        @Test
        fun `addIntegerColumn with NULL`() {
            db.addIntegerColumn(tableName = "sessions", columnName = "count", default = null)
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN count INTEGER DEFAULT NULL")
        }

        @Test
        fun `addIntegerColumn with number`() {
            db.addIntegerColumn(tableName = "sessions", columnName = "count", default = 0)
            verifyInvokedOnce(db).execSQL("ALTER TABLE sessions ADD COLUMN count INTEGER DEFAULT 0")
        }

    }

}
