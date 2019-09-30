package nerd.tuxmobil.fahrplan.congress.schedule;

import java.util.Locale;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

class TimeSegment {

    private static final String HOUR_MINUTE_DIVIDER = ":";
    private static final String TIME_PATTERN = "%02d";
    private static final int MINUTES_PER_HOUR = 60;

    private final int hour;
    private final int minute;

    TimeSegment(int minutesOfTheDay) {
        hour = minutesOfTheDay / MINUTES_PER_HOUR;
        minute = minutesOfTheDay % MINUTES_PER_HOUR;
    }

    String getFormattedText() {
        StringBuilder stringBuilder = new StringBuilder();
        String formattedHour = String.format(Locale.US, TIME_PATTERN, hour);
        String formattedMinute = String.format(Locale.US, TIME_PATTERN, minute);
        return stringBuilder
                .append(formattedHour)
                .append(HOUR_MINUTE_DIVIDER)
                .append(formattedMinute)
                .toString();
    }

    boolean isMatched(Moment moment, int offset) {
        return moment.getHour() == hour && moment.getMinute() >= minute && moment.getMinute() < minute + offset;
    }

}
