package nerd.tuxmobil.fahrplan.camp11;

import java.util.ArrayList;

import android.app.Application;

public class MyApp extends Application {
	public static Application app = null;
	public static boolean DEBUG = true;
	public static ArrayList<Lecture> lectureList = null;
	public static int numdays;
	public static String version;
	public static String title;
	public static String subtitle;
	
	enum TASKS {
		NONE,
		FETCH,
		PARSE
	}
	
	// requestCodes für startActivityForResult
	final public static int ALARMLIST = 1;
	
	public static TASKS task_running = TASKS.NONE;
	public static String fahrplan_xml;
	public static int lectureListDay = 0;
	
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        task_running = TASKS.NONE;
        lectureList = null;
    }
    
}
