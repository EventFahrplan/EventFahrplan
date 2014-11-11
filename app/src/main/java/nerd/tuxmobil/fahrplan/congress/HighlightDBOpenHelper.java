package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.HighlightsTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.HighlightsTable.Columns;

public class HighlightDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String HIGHLIGHT_TABLE_CREATE =
            "CREATE TABLE " + HighlightsTable.NAME + " (" +
                    Columns.ID + " INTEGER PRIMARY KEY, " +
                    Columns.EVENT_ID + " INTEGER," +
                    Columns.HIGHLIGHT + " INTEGER);";

    public static final String[] allcolumns = {
            Columns.ID,
            Columns.EVENT_ID,
            Columns.HIGHLIGHT
    };

    HighlightDBOpenHelper(Context context) {
        super(context, HighlightsTable.NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HIGHLIGHT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HighlightsTable.NAME);
        onCreate(db);
    }
}
