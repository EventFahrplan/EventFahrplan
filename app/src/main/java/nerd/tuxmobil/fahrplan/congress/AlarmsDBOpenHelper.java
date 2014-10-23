package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;

public class AlarmsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String ALARMS_TABLE_CREATE =
            "CREATE TABLE " + AlarmsTable.NAME + " (" +
                    AlarmsTable.Columns.ID + " INTEGER PRIMARY KEY, " +
                    AlarmsTable.Columns.EVENT_TITLE + " TEXT, " +
                    AlarmsTable.Columns.ALARM_TIME_IN_MIN + " INTEGER DEFAULT " +
                    AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT + ", " +
                    AlarmsTable.Columns.TIME + " INTEGER, " +
                    AlarmsTable.Columns.TIME_TEXT + " STRING," +
                    AlarmsTable.Columns.EVENT_ID + " INTEGER," +
                    AlarmsTable.Columns.DISPLAY_TIME + " INTEGER," +
                    AlarmsTable.Columns.DAY + " INTEGER);";

    public static final String[] allcolumns = {
            AlarmsTable.Columns.ID,
            AlarmsTable.Columns.EVENT_TITLE,
            AlarmsTable.Columns.ALARM_TIME_IN_MIN,
            AlarmsTable.Columns.TIME,
            AlarmsTable.Columns.TIME_TEXT,
            AlarmsTable.Columns.EVENT_ID,
            AlarmsTable.Columns.DISPLAY_TIME,
            AlarmsTable.Columns.DAY
    };

    AlarmsDBOpenHelper(Context context) {
        super(context, AlarmsTable.NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARMS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((oldVersion < 2) && (newVersion >= 2)) {
            db.execSQL("ALTER TABLE " + AlarmsTable.NAME + " ADD " +
                    AlarmsTable.Columns.ALARM_TIME_IN_MIN + " INTEGER DEFAULT" +
                    AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT);
        }
    }
}
