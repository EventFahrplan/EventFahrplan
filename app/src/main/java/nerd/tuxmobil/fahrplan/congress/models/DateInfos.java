package nerd.tuxmobil.fahrplan.congress.models;

import java.util.ArrayList;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

public class DateInfos extends ArrayList<DateInfo> {

    private static final long serialVersionUID = 1L;

    public boolean sameDay(Moment today, int lectureListDay) {
        Moment currentDate = today.startOfDay();
        for (DateInfo dateInfo : this) {
            if (dateInfo.getDayIdx() == lectureListDay &&
                    dateInfo.getDate().equals(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of today
     *
     * @param hourOfDayChange   Hour of day change (all lectures which start before count to the
     *                          previous day)
     * @param minuteOfDayChange Minute of day change
     * @return dayIndex if found, -1 otherwise
     */
    public int getIndexOfToday(int hourOfDayChange, int minuteOfDayChange) {
        if (isEmpty()) {
            return -1;
        }
        Moment today = new Moment();
        today.minusHours(hourOfDayChange);
        today.minusMinutes(minuteOfDayChange);

        Moment currentDate = today.startOfDay();

        int dayIndex = -1;
        for (DateInfo dateInfo : this) {
            dayIndex = dateInfo.getDayIndex(currentDate);
            if (dayIndex != -1) {
                return dayIndex;
            }
        }
        return dayIndex;
    }

}
