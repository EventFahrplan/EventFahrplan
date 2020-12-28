package nerd.tuxmobil.fahrplan.congress.schedule;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;

/**
 * Represents a segment in the time column of the main schedule view.
 */
class TimeSegment {

    // TODO Merge definition with usage in FahrplanFragment
    private static final int TIME_GRID_MINIMUM_SEGMENT_HEIGHT = 5;
    private static final int MINUTES_PER_HOUR = 60;

    private final int hour;
    private final int minute;
    private final int minutesOfTheDay;

    /**
     * The given {@code minutesOfTheDay} are normalized. The minutes value is rounded to fit
     * into the time grid determined by {@code TIME_GRID_MINIMUM_SEGMENT_HEIGHT}.
     */
    TimeSegment(int minutesOfTheDay) {
        hour = minutesOfTheDay / MINUTES_PER_HOUR;
        minute = minutesOfTheDay % MINUTES_PER_HOUR;
        int remainder = minutesOfTheDay % TIME_GRID_MINIMUM_SEGMENT_HEIGHT;
        this.minutesOfTheDay = minutesOfTheDay - remainder;
    }

    /**
     * Returns the normalized and formatted text representing the given minutes of the day.
     * This text is ready to be displayed in the time column.
     */
    String getFormattedText() {
        Moment moment = Moment.now().startOfDay().plusMinutes(minutesOfTheDay);
        return DateFormatter.newInstance().getFormattedTime24Hour(moment);
    }

    boolean isMatched(Moment moment, int offset) {
        return moment.getHour() == hour && moment.getMinute() >= minute && moment.getMinute() < minute + offset;
    }

}
