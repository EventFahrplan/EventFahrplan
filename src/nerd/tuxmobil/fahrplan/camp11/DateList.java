package nerd.tuxmobil.fahrplan.camp11;

import java.util.ArrayList;

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
}
