package nerd.tuxmobil.fahrplan.congress;

public interface FahrplanContract {

	public interface AlarmsTable {

		public static final String NAME = "alarms";

		public interface Columns {
			public static final String ID = "_id";
			public static final String EVENT_TITLE = "title";
			public static final String ALARM_TIME_IN_MIN = "alarm_time_in_min";
			public static final String TIME = "time";
			public static final String TIME_TEXT = "timeText";
			public static final String EVENT_ID = "eventid";
			public static final String DISPLAY_TIME = "displayTime";
			public static final String DAY = "day";
		}

		public interface Defaults {
			public static final int ALARM_TIME_IN_MIN_DEFAULT = -1;
		}

	}

	public interface HighlightsTable {

		public static final String NAME = "highlight";

		public interface Columns {
			public static final String ID = "_id";
			public static final String EVENT_ID = "eventid";
			public static final String HIGHLIGHT = "highlight";
		}

		public interface Values {
			public static final int HIGHLIGHT_STATE_OFF = 0;
			public static final int HIGHLIGHT_STATE_ON = 1;
		}

	}

}
