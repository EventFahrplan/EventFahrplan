package nerd.tuxmobil.fahrplan.congress;

public interface FahrplanContract {

	public interface AlarmsTable {

		public static final String NAME = "alarms";

		public interface Columns {
			public static final String ID = "_id";
			public static final String EVENT_TITLE = "title";
			public static final String TIME = "time";
			public static final String TIME_TEXT = "timeText";
			public static final String EVENT_ID = "eventid";
			public static final String DISPLAY_TIME = "displayTime";
			public static final String DAY = "day";
		}

	}

}
