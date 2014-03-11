package nerd.tuxmobil.fahrplan.congress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.format.Time;

public class DateHelper {

	public static int getDayChange(String attributeValue) {
		assert(attributeValue != null);
		String pattern = attributeValue.length() > 10 ? "yyyy-MM-dd'T'HH:mm:ssZ" : "yyyy-MM-dd";
		Date date = getDate(attributeValue, pattern);
		if (date == null) {
			return 600;	 // default
		}
		long timeUTC = date.getTime();
		Time time = new Time();
		time.set(timeUTC);
		return (time.hour * 60) + time.minute;
	}

	public static long getDateTime(final String text) {
		assert(text != null);
		String pattern = text.length() > 10 ? "yyyy-MM-dd'T'HH:mm:ssZ" : "yyyy-MM-dd";
		Date date = getDate(text, pattern);
		return date == null ? 0 : date.getTime();
	}

	protected static Date getDate(final String text, final String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
		Date date = null;
		try {
			date = dateFormat.parse(text);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

}
