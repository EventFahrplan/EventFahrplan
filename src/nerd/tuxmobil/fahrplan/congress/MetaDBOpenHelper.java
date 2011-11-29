package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MetaDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String META_TABLE_NAME = "meta";
    private static final String META_TABLE_CREATE =
                "CREATE TABLE " + META_TABLE_NAME + " (" +
                "numdays INTEGER, " +
                "version TEXT, "+
                "title TEXT, "+
                "subtitle TEXT);";
    
    public static final String[] allcolumns = { "numdays", "version", "title", "subtitle"
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
        db.execSQL("DROP TABLE IF EXISTS " + META_TABLE_NAME);
        onCreate(db);
	}
}
