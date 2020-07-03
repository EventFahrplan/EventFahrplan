package nerd.tuxmobil.fahrplan.congress.models;

import java.util.ArrayList;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;

public class DateInfos extends ArrayList<DateInfo> {

    private static final long serialVersionUID = 1L;
    /**
     * Hour of day change (all sessions which start before count to the previous day).
     */
    private static final int DAY_CHANGE_HOUR_DEFAULT = 4;
    /**
     * Minute of day change.
     */
    private static final int DAY_CHANGE_MINUTE_DEFAULT = 0;

    public boolean sameDay(Moment today, int sessionListDay) {
        Moment currentDate = today.startOfDay();
        for (DateInfo dateInfo : this) {
            if (dateInfo.getDayIdx() == sessionListDay &&
                    dateInfo.getDate().equals(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of today.
     *
     * @return dayIndex if found, {@link DateInfo#DAY_INDEX_NOT_FOUND} otherwise.
     */
    public int getIndexOfToday() {
        if (isEmpty()) {
            return DateInfo.DAY_INDEX_NOT_FOUND;
        }
        Moment today = new Moment();
        today.minusHours(DAY_CHANGE_HOUR_DEFAULT);
        today.minusMinutes(DAY_CHANGE_MINUTE_DEFAULT);

        Moment currentDate = today.startOfDay();

        int dayIndex = DateInfo.DAY_INDEX_NOT_FOUND;
        for (DateInfo dateInfo : this) {
            dayIndex = dateInfo.getDayIndex(currentDate);
            if (dayIndex != DateInfo.DAY_INDEX_NOT_FOUND) {
                return dayIndex;
            }
        }
        return dayIndex;
    }

}
