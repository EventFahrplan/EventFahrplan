package nerd.tuxmobil.fahrplan.congress.utils;

import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static String getFormattedDate(Time time) {
        StringBuilder date = new StringBuilder();
        date.append(String.format("%d", time.year));
        date.append("-");
        date.append(String.format("%02d", time.month + 1));
        date.append("-");
        date.append(String.format("%02d", time.monthDay));
        return date.toString();
    }

    public static String getFormattedTime(long time) {
        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        return dateFormat.format(new Date(time));
    }

    public static String getFormattedDate(long time) {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        return dateFormat.format(new Date(time));
    }

    public static String getFormattedDateTime(long time) {
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.FULL, SimpleDateFormat.SHORT, Locale.getDefault());
        return dateFormat.format(new Date(time));
    }

}
