package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable;

public class AlarmsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

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
        // Nothing to do here.
    }

}
