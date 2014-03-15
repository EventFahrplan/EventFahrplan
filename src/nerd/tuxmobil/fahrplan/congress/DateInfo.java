package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;

import android.text.format.Time;

public class DateInfo {
	public int dayIdx;
	public String date;

	public DateInfo(int dayIdx, String date) {
		this.dayIdx = dayIdx;
		this.date = date;
	}

	public static boolean dateInList(ArrayList<DateInfo> list, int dayIdx) {
		for (DateInfo date : list) {
			if (date.dayIdx == dayIdx) return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof DateInfo) {
			DateInfo date = (DateInfo)object;
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

	/**
	 * Returns the index of today
	 * @param dateInfos List of dates
	 * @param hourOfDayChange Hour of day change (all lectures which start before count to the previous day)
	 * @param minuteOfDayChange Minute of day change
	 * @return dayIndex if found, -1 otherwise
	 */
	public static int getIndexOfToday(DateInfos dateInfos, int hourOfDayChange, int minuteOfDayChange) {
		if (dateInfos == null || dateInfos.isEmpty()) return -1;
		Time today = new Time();
		today.setToNow();
		today.hour -= hourOfDayChange;
		today.minute -= minuteOfDayChange;

		today.normalize(true);

		StringBuilder currentDate = new StringBuilder();
		currentDate.append(String.format("%d", today.year));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.month + 1));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.monthDay));

		for (DateInfo d : dateInfos) {
			if (d.date.equals(currentDate.toString())) return d.dayIdx;
		}
		return -1;
	}

	public static boolean sameDay(Time today, int lectureListDay, DateInfos dateInfos) {
		StringBuilder currentDate = new StringBuilder();
		currentDate.append(String.format("%d", today.year));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.month + 1));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.monthDay));

		for (DateInfo d : dateInfos) {
			if ((d.dayIdx == lectureListDay) && (d.date.equals(currentDate.toString()))) return true;
		}
		return false;
	}

}
