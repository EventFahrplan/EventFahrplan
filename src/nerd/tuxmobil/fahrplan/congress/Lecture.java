package nerd.tuxmobil.fahrplan.congress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.format.Time;

public class Lecture {
	public String title;
	public String subtitle;
	public int day;
	public String room;
	public int startTime;		// minutes since day start
	public int duration;		// minutes
	public String speakers;
	public String track;
	public String lecture_id;
	public String type;
	public String lang;
	public String abstractt;
	public String description;
	public int relStartTime;
	public String links;
	public String date;
	public boolean highlight;
	public boolean has_alarm;
	public long dateUTC;
	public int room_index;
	public String recordingLicense;
	public boolean recordingOptOut;

	public Lecture(String lecture_id) {
		title = "";
		subtitle = "";
		day = 0;
		room = "";
		startTime = 0;
		duration = 0;
		speakers = "";
		track = "";
		type = "";
		lang = "";
		abstractt = "";
		description = "";
		relStartTime = 0;
		links = "";
		date = "";
		this.lecture_id = lecture_id;
		highlight = false;
		has_alarm = false;
		dateUTC = 0;
		room_index = 0;
		recordingLicense = "";
		recordingOptOut = false;
	}

	public static int parseStartTime(String text) {
		String time[] = text.split(":");
		return Integer.parseInt(time[0])*60 + Integer.parseInt(time[1]);
	}

	public static int parseDuration(String text) {
		String time[] = text.split(":");
		return Integer.parseInt(time[0])*60 + Integer.parseInt(time[1]);
	}

	public Time getTime() {
		Time t = new Time();
		String[] splitDate = date.split("-");
		t.setToNow();
		t.year = Integer.parseInt(splitDate[0]);
		t.month = Integer.parseInt(splitDate[1])-1;
		t.monthDay = Integer.parseInt(splitDate[2]);
		t.hour = relStartTime / 60;
		t.minute = relStartTime % 60;

		return t;
	}

	public static long parseDateTime(String text) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date date;
		try {
			date = df.parse(text);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
