package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MetaDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String META_TABLE_NAME = "meta";
    private static final String META_TABLE_CREATE =
                "CREATE TABLE " + META_TABLE_NAME + " (" +
                "numdays INTEGER, " +
                "version TEXT, "+
                "title TEXT, "+
                "subtitle TEXT, "+
                "day_change_hour INTEGER, "+
                "day_change_minute INTEGER, " +
                "etag TEXT);";

    public static final String[] allcolumns = { "numdays", "version", "title", "subtitle", "day_change_hour", "day_change_minute", "etag"
    };

    MetaDBOpenHelper(Context context) {
        super(context, "meta", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(META_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((oldVersion < 2) && (newVersion >= 2)) {
			db.execSQL("ALTER TABLE meta ADD COLUMN day_change_hour INTEGER DEFAULT 4");
			db.execSQL("ALTER TABLE meta ADD COLUMN day_change_minute INTEGER DEFAULT 0");
		}

		if ((oldVersion < 3) && (newVersion >= 3)) {
			db.execSQL("ALTER TABLE meta ADD COLUMN etag TEXT DEFAULT ''");
		}
	}
}
