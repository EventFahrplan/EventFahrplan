package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;

import android.text.format.Time;

public class DateList {
	public int dayIdx;
	public String date;
	
	public DateList(int dayIdx, String date) {
		this.dayIdx = dayIdx;
		this.date = date;
	}
	
	public static boolean dateInList(ArrayList<DateList> list, int dayIdx) {
		for (DateList date : list) {
			if (date.dayIdx == dayIdx) return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param list	DateList list
	 * @param hourOfDayChange Hour of day change (all lectures which start before count to the previous day)
	 * @param minuteOfDayChange Minute of day change
	 * @return dayIndex if found, -1 otherwise
	 */
	public static int getIndexOfToday(ArrayList<DateList> list, int hourOfDayChange, int minuteOfDayChange) {
		if (list == null) return -1;
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
		
		for (DateList d : list) {
			if (d.date.equals(currentDate.toString())) return d.dayIdx;
		}
		return -1;
	}

	public static boolean sameDay(Time today, int lectureListDay, ArrayList<DateList> list) {
		StringBuilder currentDate = new StringBuilder();
		currentDate.append(String.format("%d", today.year));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.month + 1));
		currentDate.append("-");
		currentDate.append(String.format("%02d", today.monthDay));
		
		for (DateList d : list) {
			if ((d.dayIdx == lectureListDay) && (d.date.equals(currentDate.toString()))) return true;
		}
		return false;
	}
}
