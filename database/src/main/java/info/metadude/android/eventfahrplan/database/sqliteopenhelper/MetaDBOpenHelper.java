package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults;
import info.metadude.android.eventfahrplan.database.extensions.SQLiteDatabaseExtensions;

public class MetaDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;

    private static final String DATABASE_NAME = "meta";

    private static final String META_TABLE_CREATE =
            "CREATE TABLE " + MetasTable.NAME + " (" +
                    Columns.NUM_DAYS + " INTEGER, " +
                    Columns.VERSION + " TEXT, " +
                    Columns.TITLE + " TEXT, " +
                    Columns.SUBTITLE + " TEXT, " +
                    Columns.ETAG + " TEXT, " +
                    Columns.TIME_ZONE_NAME + " TEXT, " +
                    Columns.SCHEDULE_LAST_MODIFIED + " TEXT DEFAULT '');";

    public MetaDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(META_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                    Columns.ETAG + " TEXT DEFAULT " + Defaults.ETAG_DEFAULT);
        }
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                    Columns.TIME_ZONE_NAME + " TEXT");
        }
        if (oldVersion < 4) {
            // Clear database from 34C3.
            db.execSQL("DROP TABLE IF EXISTS " + MetasTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 5) {
            // Clear database from 35C3 & Camp 2019.
            db.execSQL("DROP TABLE IF EXISTS " + MetasTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 7) {
            // Clear database from rC3 12/2020.
            db.execSQL("DROP TABLE IF EXISTS " + MetasTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 8) {
            // Clear database from rC3 NOWHERE 12/2021 & 36C3 2019.
            db.execSQL("DROP TABLE IF EXISTS " + MetasTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 9) {
            boolean columnExists = SQLiteDatabaseExtensions.columnExists(db, MetasTable.NAME, Columns.SCHEDULE_LAST_MODIFIED);
            if (!columnExists) {
                db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                        Columns.SCHEDULE_LAST_MODIFIED + " TEXT DEFAULT ''");
            }
        }
    }
}
