package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable;

public class AlarmsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_NAME = "alarms";

    private static final String ALARMS_TABLE_CREATE =
            "CREATE TABLE " + AlarmsTable.NAME + " (" +
                    AlarmsTable.Columns.ID + " INTEGER PRIMARY KEY, " +
                    AlarmsTable.Columns.SESSION_TITLE + " TEXT, " +
                    AlarmsTable.Columns.ALARM_TIME_IN_MIN + " INTEGER DEFAULT " +
                    AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT + ", " +
                    AlarmsTable.Columns.TIME + " INTEGER, " +
                    AlarmsTable.Columns.TIME_TEXT + " STRING," +
                    AlarmsTable.Columns.SESSION_ID + " INTEGER," +
                    AlarmsTable.Columns.DISPLAY_TIME + " INTEGER," +
                    AlarmsTable.Columns.DAY + " INTEGER);";

    public AlarmsDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARMS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + AlarmsTable.NAME + " ADD " +
                    AlarmsTable.Columns.ALARM_TIME_IN_MIN + " INTEGER DEFAULT" +
                    AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT);
        }
        if (oldVersion < 3) {
            // Clear database from 34C3.
            db.execSQL("DROP TABLE IF EXISTS " + AlarmsTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 4) {
            // Clear database from 35C3.
            db.execSQL("DROP TABLE IF EXISTS " + AlarmsTable.NAME);
            onCreate(db);
        }
        if (oldVersion < 5) {
            // Clear database from rC3 12/2020.
            db.execSQL("DROP TABLE IF EXISTS " + AlarmsTable.NAME);
            onCreate(db);
        }
    }
}
