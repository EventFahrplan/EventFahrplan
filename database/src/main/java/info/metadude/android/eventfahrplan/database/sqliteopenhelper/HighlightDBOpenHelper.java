package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.HighlightsTable.Columns;

public class HighlightDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_NAME = "highlight";

    private static final String HIGHLIGHT_TABLE_CREATE =
            "CREATE TABLE " + HighlightsTable.NAME + " (" +
                    Columns.ID + " INTEGER PRIMARY KEY, " +
                    Columns.SESSION_ID + " INTEGER," +
                    Columns.HIGHLIGHT + " INTEGER);";

    public HighlightDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HIGHLIGHT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Clear database from 36C3 2019.
        db.execSQL("DROP TABLE IF EXISTS " + HighlightsTable.NAME);
        onCreate(db);
    }
}
