package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults;

public class MetaDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "meta";

    private static final String META_TABLE_CREATE =
            "CREATE TABLE " + MetasTable.NAME + " (" +
                    Columns.NUM_DAYS + " INTEGER, " +
                    Columns.VERSION + " TEXT, " +
                    Columns.TITLE + " TEXT, " +
                    Columns.SUBTITLE + " TEXT, " +
                    Columns.DAY_CHANGE_HOUR + " INTEGER, " +
                    Columns.DAY_CHANGE_MINUTE + " INTEGER, " +
                    Columns.ETAG + " TEXT);";

    public MetaDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(META_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                    Columns.DAY_CHANGE_HOUR + " INTEGER DEFAULT " +
                    Defaults.DAY_CHANGE_HOUR_DEFAULT);
            db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                    Columns.DAY_CHANGE_MINUTE + " INTEGER DEFAULT " +
                    Defaults.DAY_CHANGE_MINUTE_DEFAULT);
        }

        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE " + MetasTable.NAME + " ADD COLUMN " +
                    Columns.ETAG + " TEXT DEFAULT " + Defaults.ETAG_DEFAULT);
        }
        if (oldVersion < 4) {
            // Clear database from 34C3.
            db.execSQL("DROP TABLE IF EXISTS " + MetasTable.NAME);
            onCreate(db);
        }
    }
}
