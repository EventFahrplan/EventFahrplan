package nerd.tuxmobil.fahrplan.congress.utils;

import android.support.annotation.NonNull;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

public class DateHelper {

    /**
     * Returns a formatted string for the current date. Pattern YYYY-MM-DD.
     */
    public static String getCurrentDate() {
        return getFormattedDate(new Moment());
    }

    /**
     * Returns a formatted string for the given moment. Pattern YYYY-MM-DD.
     */
    public static String getFormattedDate(@NonNull Moment moment) {
        StringBuilder date = new StringBuilder();
        date.append(String.format("%d", moment.getYear()));
        date.append("-");
        date.append(String.format("%02d", moment.getMonth() + 1));
        date.append("-");
        date.append(String.format("%02d", moment.getMonthDay()));
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

    /**
     * Returns the LocalDate parsed from the given date string and pattern.
     * Example "2019-08-21", "yyyy-MM-dd" -> 2019-08-21
     */
    public static LocalDate getLocalDate(@NonNull final String dateString, @NonNull final String pattern) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Returns the ZonedDateTime representing the start of the day.
     * Example: 2019-08-21, +02:00 -> 2019-08-21T00:00:00+02:00
     */
    public static ZonedDateTime getDayStartsAtDate(@NonNull final LocalDate date, ZoneOffset timeZoneOffset) {
        return ZonedDateTime.of(date, LocalTime.of(0, 0, 0, 0), timeZoneOffset);
    }

    /**
     * Returns the ZonedDateTime representing the end of the day.
     * Example: 2019-08-21T13:42:49+02:00 -> 2019-08-21T23:59:59+02:00
     */
    public static ZonedDateTime getDayEndsAtDate(@NonNull final ZonedDateTime date) {
        return date
                // Zero all time fields
                .minusHours(date.getHour())
                .minusMinutes(date.getMinute())
                .minusSeconds(date.getSecond())
                .minus(date.get(ChronoField.MILLI_OF_SECOND), ChronoUnit.MILLIS)
                // Shift to the end of this day
                .plusDays(1)
                .minusSeconds(1);
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
        Moment moment = new Moment(dateUtc);
        return moment.getHour() * 60 + moment.getMinute();
    }

    public static int getDayOfMonth(long dateUtc) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(dateUtc);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

}
