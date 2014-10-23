package nerd.tuxmobil.fahrplan.congress;


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

    @Override
    public boolean equals(Object object) {
        if (object instanceof DateInfo) {
            DateInfo date = (DateInfo) object;
            return super.equals(object) &&
                    date.dayIdx == dayIdx &&
                    date.date.equals(date);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 13 | dayIdx + date.hashCode() * 7;
    }

    @Override
    public String toString() {
        return "dayIndex = " + dayIdx + ", date = " + date;
    }

}
