package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionByNotificationIdTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Defaults;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Values;
import info.metadude.android.eventfahrplan.database.extensions.SQLiteDatabaseExtensions;

public class SessionsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 14;

    private static final String DATABASE_NAME = "lectures"; // Keep table name to avoid database migration.

    private static final String SESSIONS_TABLE_CREATE =
            "CREATE TABLE " + SessionsTable.NAME + " (" +
                    Columns.SESSION_ID + " TEXT, " +
                    Columns.TITLE + " TEXT, " +
                    Columns.SUBTITLE + " TEXT, " +
                    Columns.DAY + " INTEGER, " +
                    Columns.ROOM_NAME + " STRING, " +
                    Columns.ROOM_IDENTIFIER + " TEXT, " +
                    Columns.SLUG + " TEXT, " +
                    Columns.START + " INTEGER, " +
                    Columns.DURATION + " INTEGER, " +
                    Columns.SPEAKERS + " STRING, " +
                    Columns.TRACK + " STRING, " +
                    Columns.TYPE + " STRING, " +
                    Columns.LANG + " STRING, " +
                    Columns.ABSTRACT + " STRING, " +
                    Columns.DESCR + " STRING, " +
                    Columns.REL_START + " INTEGER, " +
                    Columns.DATE + " STRING, " +
                    Columns.LINKS + " STRING, " +
                    Columns.DATE_UTC + " INTEGER, " +
                    Columns.TIME_ZONE_OFFSET + " INTEGER DEFAULT NULL, " +
                    Columns.ROOM_INDEX + " INTEGER, " +
                    Columns.REC_LICENSE + " STRING, " +
                    Columns.REC_OPTOUT + " INTEGER," +
                    Columns.URL + " TEXT," +
                    Columns.CHANGED_TITLE + " INTEGER," +
                    Columns.CHANGED_SUBTITLE + " INTEGER," +
                    Columns.CHANGED_ROOM_NAME + " INTEGER," +
                    Columns.CHANGED_DAY + " INTEGER," +
                    Columns.CHANGED_SPEAKERS + " INTEGER," +
                    Columns.CHANGED_RECORDING_OPTOUT + " INTEGER," +
                    Columns.CHANGED_LANGUAGE + " INTEGER," +
                    Columns.CHANGED_TRACK + " INTEGER," +
                    Columns.CHANGED_IS_NEW + " INTEGER," +
                    Columns.CHANGED_TIME + " INTEGER," +
                    Columns.CHANGED_DURATION + " INTEGER," +
                    Columns.CHANGED_IS_CANCELED + " INTEGER)";

    /**
     * Create statement for a mapping table (notification ID, session ID). Each insert automatically
     * increments the primary key and therefore generates a new notification ID.
     */
    private static final String SESSION_BY_NOTIFICATION_ID_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + SessionByNotificationIdTable.NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            SessionByNotificationIdTable.Columns.SESSION_ID + " TEXT)";

    public SessionsDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(SESSIONS_TABLE_CREATE);
        db.execSQL(SESSION_BY_NOTIFICATION_ID_TABLE_CREATE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME +
                    " ADD COLUMN " + Columns.DATE_UTC + " INTEGER DEFAULT " +
                    Defaults.DATE_UTC_DEFAULT);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME +
                    " ADD COLUMN " + Columns.ROOM_INDEX + " INTEGER DEFAULT " +
                    Defaults.ROOM_IDX_DEFAULT);
        }
        if (oldVersion < 4 && newVersion >= 4) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME +
                    " ADD COLUMN " + Columns.REC_LICENSE + " STRING DEFAULT ''");
            db.execSQL("ALTER TABLE " + SessionsTable.NAME +
                    " ADD COLUMN " + Columns.REC_OPTOUT + " INTEGER DEFAULT " +
                    Values.REC_OPT_OUT_OFF);
        }
        if (oldVersion < 5 && newVersion >= 5) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_TITLE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_SUBTITLE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_ROOM_NAME + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_DAY + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_SPEAKERS + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_RECORDING_OPTOUT + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_LANGUAGE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_TRACK + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_IS_NEW + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_TIME + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_DURATION + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.CHANGED_IS_CANCELED + " INTEGER DEFAULT " + 0);
        }
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.SLUG + " TEXT DEFAULT ''");
        }
        if (oldVersion < 7 && newVersion >= 7) {
            db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.URL + " TEXT DEFAULT ''");
        }
        if (oldVersion < 8) {
            // Clear database from 34C3.
            db.execSQL("DROP TABLE IF EXISTS " + SessionsTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 9) {
            // Clear database from 35C3 & Camp 2019.
            db.execSQL("DROP TABLE IF EXISTS " + SessionsTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 10 && newVersion >= 10) {
            db.execSQL(SESSION_BY_NOTIFICATION_ID_TABLE_CREATE);
        }
        if (oldVersion < 11 && newVersion >= 11) {
            boolean columnExists = SQLiteDatabaseExtensions.columnExists(db, SessionsTable.NAME, Columns.TIME_ZONE_OFFSET);
            if (!columnExists) {
                db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.TIME_ZONE_OFFSET + " INTEGER DEFAULT NULL");
            }
        }
        if (oldVersion < 12) {
            // Clear database from rC3 12/2020.
            db.execSQL("DROP TABLE IF EXISTS " + SessionsTable.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SessionByNotificationIdTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 13) {
            // Clear database from rC3 NOWHERE 12/2021.
            db.execSQL("DROP TABLE IF EXISTS " + SessionsTable.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SessionByNotificationIdTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 14) {
            boolean columnExists = SQLiteDatabaseExtensions.columnExists(db, SessionsTable.NAME, Columns.ROOM_IDENTIFIER);
            if (!columnExists) {
                db.execSQL("ALTER TABLE " + SessionsTable.NAME + " ADD COLUMN " + Columns.ROOM_IDENTIFIER + " TEXT DEFAULT ''");
            }
        }

    }
}
