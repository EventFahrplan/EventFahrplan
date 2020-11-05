package nerd.tuxmobil.fahrplan.congress.schedule;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;

class TimeSegment {

    private static final int MINUTES_PER_HOUR = 60;

    private final int hour;
    private final int minute;
    private final int minutesOfTheDay;

    TimeSegment(int minutesOfTheDay) {
        hour = minutesOfTheDay / MINUTES_PER_HOUR;
        minute = minutesOfTheDay % MINUTES_PER_HOUR;
        this.minutesOfTheDay = minutesOfTheDay;
    }

    String getFormattedText() {
        Moment moment = Moment.now().startOfDay();
        moment.plusMinutes(minutesOfTheDay);
        return DateFormatter.newInstance().getFormattedTime24Hour(moment);
    }

    boolean isMatched(Moment moment, int offset) {
        return moment.getHour() == hour && moment.getMinute() >= minute && moment.getMinute() < minute + offset;
    }

}
