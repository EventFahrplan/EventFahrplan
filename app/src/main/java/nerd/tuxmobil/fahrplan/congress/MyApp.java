package nerd.tuxmobil.fahrplan.congress;

import android.app.Application;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import org.ligi.tracedroid.TraceDroid;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import nerd.tuxmobil.fahrplan.congress.models.DateInfos;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.utils.ConferenceTimeFrame;

public class MyApp extends Application {

    public static Application app = null;

    public static final boolean DEBUG = false;

    public static List<Lecture> lectureList = null;

    public static Meta meta;

    public static DateInfos dateInfos = null;

    private static final long first_day_start = getMilliseconds("Europe/Paris",
            BuildConfig.SCHEDULE_FIRST_DAY_START_YEAR,
            BuildConfig.SCHEDULE_FIRST_DAY_START_MONTH,
            BuildConfig.SCHEDULE_FIRST_DAY_START_DAY);

    private static final long last_day_end = getMilliseconds("Europe/Paris",
            BuildConfig.SCHEDULE_LAST_DAY_END_YEAR,
            BuildConfig.SCHEDULE_LAST_DAY_END_MONTH,
            BuildConfig.SCHEDULE_LAST_DAY_END_DAY);

    public static final ConferenceTimeFrame conferenceTimeFrame =
            new ConferenceTimeFrame(first_day_start, last_day_end);

    public static int roomCount = 0;

    /**
     * maps room title to room index
     */
    public static final Map<String, Integer> roomsMap = new HashMap<>();

    /**
     * maps column index to room index
     */
    public static SparseArrayCompat<Integer> roomList = new SparseArrayCompat<>();

    public enum TASKS {
        NONE,
        FETCH,
        PARSE,
        FETCH_CANCELLED
    }

    // requestCodes für startActivityForResult
    public final static int ALARMLIST = 1;
    public final static int EVENTVIEW = 2;
    public final static int CHANGELOG = 3;
    public final static int STARRED = 4;
    public final static int SETTINGS = 5;

    public static TASKS task_running = TASKS.NONE;

    public static int lectureListDay = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        TraceDroid.init(this);
        app = this;
        task_running = TASKS.NONE;
        lectureList = null;
        AppRepository.INSTANCE.initialize(
                getApplicationContext(),
                Logging.Companion.get()
        );
    }

    private static long getMilliseconds(String timeZoneId, int year, int month, int day) {
        TimeZone zone = TimeZone.getTimeZone(timeZoneId);
        Calendar calendar = new GregorianCalendar(zone);
        int zeroBasedMonth = month - 1;
        calendar.set(year, zeroBasedMonth, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static void LogDebug(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }
}
