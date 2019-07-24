package nerd.tuxmobil.fahrplan.congress.models;


public class DateInfo {

    public int dayIdx;

    public String date;

    public DateInfo(int dayIdx, String date) {
        this.dayIdx = dayIdx;
        this.date = date;
    }

    public int getDayIndex(String date) {
        return this.date.equals(date) ? dayIdx : -1;
    }

    @SuppressWarnings({"SimplifiableIfStatement", "EqualsReplaceableByObjectsCall"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateInfo dateInfo = (DateInfo) o;
        if (dayIdx != dateInfo.dayIdx) {
            return false;
        }
        return date != null ? date.equals(dateInfo.date) : dateInfo.date == null;
    }

    @Override
    public int hashCode() {
        int result = dayIdx;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "dayIndex = " + dayIdx + ", date = " + date;
    }

}
