package nerd.tuxmobil.fahrplan.congress.utils;

import android.support.annotation.NonNull;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

public class DateHelper {

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

    /**
     * Returns the duration in minutes between the given dates.
     * Example: 2019-08-25T12:00:00+00:00, 2019-08-25T12:30:13+00:00 -> 30L
     */
    public static long getDurationMinutes(@NonNull final ZonedDateTime startsAt, @NonNull final ZonedDateTime endsAt) {
        return Duration.between(startsAt, endsAt).toMinutes();
    }

    /**
     * Returns the minute of the day for the given date.
     * Example: 2019-08-27T00:06:30+00:00 -> 390
     */
    public static int getMinuteOfDay(@NonNull final ZonedDateTime date) {
        return date.get(ChronoField.MINUTE_OF_DAY);
    }

    public static int getMinutesOfDay(long dateUtc) {
        return new Moment(dateUtc).getMinute();
    }

    // TODO was utc, now zoned. But is this correct? getMinuteOfDay, getMinutesOfDay: was and still is zoned. Shouldn't all of them be UTC?
    public static int getDayOfMonth(long dateUtc) {
        return new Moment(dateUtc).getMonthDay();
    }
}
