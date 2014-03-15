package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Application;
import android.util.Log;
import android.util.SparseIntArray;

public class MyApp extends Application {
	public static Application app = null;
	public static boolean DEBUG = false;
	public static ArrayList<Lecture> lectureList = null;
	public static int numdays;
	public static String version;
	public static String title;
	public static String subtitle;
	public static DateInfos dateList = null;
	public static FetchFahrplan fetcher = null;
	public static FahrplanParser parser = null;
	public static String schedulePath = "/congress/2013/Fahrplan/schedule.xml";
	// Ruby: Date.new(2013,12,27).to_time.to_i
	public static long first_day_start = 1388098800000l;
	// Ruby: Date.new(2013,12,31).to_time.to_i
	public static long last_day_end = 1388444400000l;
	public static int room_count = 0;
	public static HashMap<String, Integer> roomsMap = new HashMap<String, Integer>();

	enum TASKS {
		NONE,
		FETCH,
		PARSE,
		FETCH_CANCELLED
	}

	// requestCodes für startActivityForResult
	final public static int ALARMLIST = 1;
	final public static int EVENTVIEW = 2;

	public static TASKS task_running = TASKS.NONE;
	public static String fahrplan_xml;
	public static int lectureListDay = 0;
	public static int dayChangeHour;
	public static int dayChangeMinute;
	public static String eTag;
	public static SparseIntArray roomList = new SparseIntArray();

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        task_running = TASKS.NONE;
        lectureList = null;
    }

	public static void LogDebug(String tag, String message) {
		if (DEBUG == true) {
			Log.d(tag, message);
		}
	}
}
