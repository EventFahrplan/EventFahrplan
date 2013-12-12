package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LecturesDBOpenHelper extends SQLiteOpenHelper {

	private final String LOG_TAG = "DBHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String LECTURES_TABLE_NAME = "lectures";
    private static final String LECTURES_TABLE_CREATE =
                "CREATE TABLE " + LECTURES_TABLE_NAME + " (" +
                "event_id TEXT, " +
                "title TEXT, " +
                "subtitle TEXT, " +
                "day INTEGER, " +
                "room STRING, " +
                "start INTEGER, " +
                "duration INTEGER, " +
                "speakers STRING, " +
                "track STRING, " +
                "type STRING, " +
                "lang STRING, " +
                "abstract STRING, " +
                "descr STRING, " +
                "relStart INTEGER, " +
                "date STRING, " +
                "links STRING);";
    
    public static final String[] allcolumns = { "event_id", "title", "subtitle", "day", "room", "start",
    	"duration", "speakers", "track", "type", "lang", "abstract", "descr", "relStart", "date", "links"
    };

    LecturesDBOpenHelper(Context context) {
        super(context, "lectures", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LECTURES_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + LECTURES_TABLE_NAME);
        onCreate(db);
	}
}
