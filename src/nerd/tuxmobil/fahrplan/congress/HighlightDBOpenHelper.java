package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighlightDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String HIGHLIGHT_TABLE_NAME = "highlight";
    private static final String HIGHLIGHT_TABLE_CREATE =
                "CREATE TABLE " + HIGHLIGHT_TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY, " +
                "eventid INTEGER," +
                "highlight INTEGER);";
    
    public static final String[] allcolumns = { "_id", "eventid", "highlight"
    };

    HighlightDBOpenHelper(Context context) {
        super(context, "highlight", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HIGHLIGHT_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HIGHLIGHT_TABLE_NAME);
        onCreate(db);
	}
}
