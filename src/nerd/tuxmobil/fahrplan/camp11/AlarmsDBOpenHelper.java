package nerd.tuxmobil.fahrplan.camp11;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmsDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String ALARMS_TABLE_NAME = "alarms";
    private static final String ALARMS_TABLE_CREATE =
                "CREATE TABLE " + ALARMS_TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY, " +
                "title TEXT, "+
                "time INTEGER, "+
                "timeText STRING," +
                "eventid INTEGER," +
                "displayTime INTEGER," +
                "day INTEGER);";
    
    public static final String[] allcolumns = { "_id", "title", "time", "timeText", "eventid", "displayTime", "day"
    };

    AlarmsDBOpenHelper(Context context) {
        super(context, "alarms", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARMS_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ALARMS_TABLE_NAME);
        onCreate(db);
	}
}
