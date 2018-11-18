package nerd.tuxmobil.fahrplan.congress.utils;

public class ConferenceTimeFrame {

    private final long firstDayStartTime;

    private final long lastDayEndTime;

    public ConferenceTimeFrame(long firstDayStartTime, long lastDayEndTime) {
        this.firstDayStartTime = firstDayStartTime;
        this.lastDayEndTime = lastDayEndTime;
        if (!isValid()) {
            throw new IllegalStateException("Invalid conference time frame: " + this);
        }
    }

    boolean isValid() {
        return Long.compare(firstDayStartTime, lastDayEndTime) == -1;
    }

    public long getFirstDayStartTime() {
        return firstDayStartTime;
    }

    public boolean contains(long time) {
        return startsAtOrBefore(time) && time < lastDayEndTime;
    }

    public boolean endsBefore(long time) {
        return time >= lastDayEndTime;
    }

    public boolean startsAfter(long time) {
        return time < firstDayStartTime;
    }

    public boolean startsAtOrBefore(long time) {
        return time >= firstDayStartTime;
    }

    @Override
    public String toString() {
        return "firstDayStartTime = " + firstDayStartTime + ", lastDayEndTime = " + lastDayEndTime;
    }

}
