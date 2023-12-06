package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionByNotificationIdTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.SessionsTable.Columns;

public class SessionsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "sessions";

    private static final String SESSIONS_TABLE_CREATE =
            "CREATE TABLE " + SessionsTable.NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.SESSION_ID + " TEXT, " +
                    Columns.GUID + " TEXT NOT NULL UNIQUE, " +
                    Columns.TITLE + " TEXT, " +
                    Columns.SUBTITLE + " TEXT, " +
                    Columns.DAY + " INTEGER, " +
                    Columns.ROOM + " STRING, " +
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
                    Columns.ROOM_IDX + " INTEGER, " +
                    Columns.REC_LICENSE + " STRING, " +
                    Columns.REC_OPTOUT + " INTEGER," +
                    Columns.URL + " TEXT," +
                    Columns.CHANGED_TITLE + " INTEGER," +
                    Columns.CHANGED_SUBTITLE + " INTEGER," +
                    Columns.CHANGED_ROOM + " INTEGER," +
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
        // Nothing to do here.
    }

}
