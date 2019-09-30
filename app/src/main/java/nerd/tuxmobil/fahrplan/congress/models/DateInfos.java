package nerd.tuxmobil.fahrplan.congress.models;

import java.util.ArrayList;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class DateInfos extends ArrayList<DateInfo> {

    private static final long serialVersionUID = 1L;

    public boolean sameDay(Moment moment, int lectureListDay) {
        String currentDate = DateHelper.getFormattedDate(moment);
        for (DateInfo dateInfo : this) {
            if (dateInfo.dayIdx == lectureListDay &&
                    dateInfo.date.equals(currentDate)) {
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
        today.normalize();

        String currentDate = DateHelper.getFormattedDate(today);

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
