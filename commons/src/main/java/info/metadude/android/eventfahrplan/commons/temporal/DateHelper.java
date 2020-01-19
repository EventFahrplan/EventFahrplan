package info.metadude.android.eventfahrplan.commons.temporal;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    private static DateFormat timeShort = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    private static DateFormat dateShort = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
    private static DateFormat dateTimeShort = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
    private static DateFormat dateTimeFull = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT);

    public static String getFormattedTime(long time) {
        return timeShort.format(new Date(time));
    }

    public static String getFormattedDate(long time) {
        return dateShort.format(new Date(time));
    }

    public static String getFormattedDateTime(long time) {
        return dateTimeFull.format(new Date(time));
    }

    public static String getFormattedDateTimeShort(long time) {
        return dateTimeShort.format(new Date(time));
    }

    /**
     * Returns the duration in minutes between the given dates.
     * Example: 2019-08-25T12:00:00+00:00, 2019-08-25T12:30:13+00:00 -> 30L
     */
    public static long getDurationMinutes(final ZonedDateTime startsAt, final ZonedDateTime endsAt) {
        return Duration.between(startsAt, endsAt).toMinutes();
    }

    /**
     * Returns the minute of the day for the given zoned date time. Zone offset is respected!
     * Note: Lecture.startTime (minutes since day start) is always based on UTC. Hence, its safe to use this method to compare with Lecture data.
     * <p>
     * Example: 2019-08-27T00:06:30+04:00 -> 150
     */
    public static int getMinuteOfDay(final ZonedDateTime date) {
        return new Moment(date).getMinuteOfDay();
    }

    /**
     * Returns the minute of the day for the given UTC timestamp. Zone offset is respected!
     * Note: Lecture.startTime (minutes since day start) is always based on UTC. Hence, it's safe to use this method to compare with Lecture data.
     * <p>
     * Example: 2019-08-27T00:06:30+04:00 -> 150 => (6-4) * 60 + 30
     */
    public static int getMinuteOfDay(long dateUtc) {
        return new Moment(dateUtc).getMinuteOfDay();
    }

    public static int getDayOfMonth(long dateUtc) {
        return new Moment(dateUtc).getMonthDay();
    }
}
