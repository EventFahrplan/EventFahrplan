package nerd.tuxmobil.fahrplan.congress;

import android.text.format.Time;

import java.util.ArrayList;

public class DateInfos extends ArrayList<DateInfo> {

    private static final long serialVersionUID = 1L;

    public boolean sameDay(Time today, int lectureListDay) {
        String currentDate = DateHelper.getFormattedDate(today);
        for (DateInfo dateInfo : this) {
            if ((dateInfo.dayIdx == lectureListDay) &&
                    (dateInfo.date.equals(currentDate))) {
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
        Time today = new Time();
        today.setToNow();
        today.hour -= hourOfDayChange;
        today.minute -= minuteOfDayChange;

        today.normalize(true);

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
