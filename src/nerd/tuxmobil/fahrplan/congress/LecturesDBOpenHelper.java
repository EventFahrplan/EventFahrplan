package nerd.tuxmobil.fahrplan.congress;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Columns;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Values;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Defaults;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LecturesDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String LECTURES_TABLE_CREATE =
                "CREATE TABLE " + LecturesTable.NAME + " (" +
                Columns.EVENT_ID + " TEXT, " +
                Columns.TITLE + " TEXT, " +
                Columns.SUBTITLE + " TEXT, " +
                Columns.DAY + " INTEGER, " +
                Columns.ROOM + " STRING, " +
                Columns.START + " INTEGER, " +
                Columns.DURATION + " INTEGER, " +
                Columns.SPEAKERS + " STRING, " +
                Columns.TRACK + " STRING, " +
                Columns.TYPE + " STRING, " +
                Columns.LANG + " STRING, " +
                Columns.ABSTRACT + " STRING, " +
                Columns.DESCR + " STRING, " +
                Columns.REL_START + " INTEGER, " +
                Columns.DATE + " STRING, " +
                Columns.LINKS + " STRING, " +
                Columns.DATE_UTC + " INTEGER, " +
                Columns.ROOM_IDX + " INTEGER, " +
                Columns.REC_LICENSE + " STRING, " +
                Columns.REC_OPTOUT + " INTEGER);";

    public static final String[] allcolumns = {
    	Columns.EVENT_ID,
    	Columns.TITLE,
    	Columns.SUBTITLE,
    	Columns.DAY,
    	Columns.ROOM,
    	Columns.START,
    	Columns.DURATION,
    	Columns.SPEAKERS,
    	Columns.TRACK,
    	Columns.TYPE,
    	Columns.LANG,
    	Columns.ABSTRACT,
    	Columns.DESCR,
    	Columns.REL_START,
    	Columns.DATE,
    	Columns.LINKS,
    	Columns.DATE_UTC,
    	Columns.ROOM_IDX,
        Columns.REC_LICENSE,
        Columns.REC_OPTOUT
    };

    LecturesDBOpenHelper(Context context) {
        super(context, LecturesTable.NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LECTURES_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((oldVersion < 2) && (newVersion >= 2)) {
			db.execSQL("ALTER TABLE " + LecturesTable.NAME +
				" ADD COLUMN " + Columns.DATE_UTC + " INTEGER DEFAULT " +
				Defaults.DATE_UTC_DEFAULT);
		}
		if ((oldVersion < 3) && (newVersion >= 3)) {
			db.execSQL("ALTER TABLE " + LecturesTable.NAME +
				" ADD COLUMN " + Columns.ROOM_IDX + " INTEGER DEFAULT " +
				Defaults.ROOM_IDX_DEFAULT);
		}
		if ((oldVersion < 4) && (newVersion >= 4)) {
			db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                " ADD COLUMN " + Columns.REC_LICENSE + " STRING DEFAULT ''");
			db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                " ADD COLUMN " + Columns.REC_OPTOUT  + " INTEGER DEFAULT " +
                Values.REC_OPTOUT_OFF);
		}
	}
}
