package nerd.tuxmobil.fahrplan.congress;

import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static boolean dateIsWithinRange(Date date, Date[] dateRange) {
        Date oldest = dateRange[0];
        Date newest = dateRange[1];
        return ((date.equals(oldest) || date.after(oldest)) && (date.equals(newest) || date
                .before(newest)));
    }

    public static String getFormattedDate(Time time) {
        StringBuilder date = new StringBuilder();
        date.append(String.format("%d", time.year));
        date.append("-");
        date.append(String.format("%02d", time.month + 1));
        date.append("-");
        date.append(String.format("%02d", time.monthDay));
        return date.toString();
    }

    /**
     * Returns a formatted time string.
     * Try this pattern for readable output: %Y-%m-%dT%H:%M:%S%z
     */
    public static String getFormattedTime(long timeInSeconds, final String pattern) {
        Time time = new Time();
        time.set(timeInSeconds);
        return time.format(pattern);
    }

    /**
     * Returns a formatted date string.
     * This pattern is used: yyyy-MM-dd'T'HH:mm:ssZ
     */
    public static String getFormattedDate(final Date date) {
        return getFormattedDate(date, "yyyy-MM-dd'T'HH:mm:ssZ");
    }

    /**
     * Returns a formatted date string.
     */
    public static String getFormattedDate(final Date date, final String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        return dateFormat.format(date);
    }

    public static int getDayChange(String attributeValue) {
        assert (attributeValue != null);
        String pattern = attributeValue.length() > 10 ? "yyyy-MM-dd'T'HH:mm:ssZ" : "yyyy-MM-dd";
        Date date = getDate(attributeValue, pattern);
        if (date == null) {
            return 600;         // default
        }
        long timeUTC = date.getTime();
        Time time = new Time();
        time.set(timeUTC);
        return (time.hour * 60) + time.minute;
    }

    public static long getDateTime(final String text) {
        assert (text != null);
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
