package nerd.tuxmobil.fahrplan.congress;

public interface FahrplanContract {

	public interface MetasTable {

		public static final String NAME = "meta";

		public interface Columns {
			public static final String NUM_DAYS = "numdays";
			public static final String VERSION = "version";
			public static final String TITLE = "title";
			public static final String SUBTITLE = "subtitle";
			public static final String DAY_CHANGE_HOUR = "day_change_hour";
			public static final String DAY_CHANGE_MINUTE = "day_change_minute";
			public static final String ETAG = "etag";
		}

		public interface Defaults {
			public static final int NUM_DAYS_DEFAULT = 0;
			public static final int DAY_CHANGE_HOUR_DEFAULT = 4;
			public static final int DAY_CHANGE_MINUTE_DEFAULT = 0;
			public static final String ETAG_DEFAULT = "''";
		}

	}

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

	public interface LecturesTable {

		public static final String NAME = "lectures";

		public interface Columns {
			public static final String EVENT_ID = "event_id";
			public static final String TITLE = "title";
			public static final String SUBTITLE = "subtitle";
			public static final String DAY = "day";
			public static final String ROOM = "room";
			public static final String START = "start";
			public static final String DURATION = "duration";
			public static final String SPEAKERS = "speakers";
			public static final String TRACK = "track";
			public static final String TYPE = "type";
			public static final String LANG = "lang";
			public static final String ABSTRACT = "abstract";
			public static final String DESCR = "descr";
			public static final String REL_START = "relStart";
			public static final String DATE = "date";
			public static final String LINKS = "links";
			public static final String DATE_UTC = "dateUTC";
			public static final String ROOM_IDX = "room_idx";
			public static final String REC_LICENSE = "rec_license";
			public static final String REC_OPTOUT = "rec_optout";
		}

		public interface Defaults {
			public static final int DATE_UTC_DEFAULT = 0;
			public static final int ROOM_IDX_DEFAULT = 0;
		}

		public interface Values {
			public static final int REC_OPTOUT_OFF = 0;
			public static final int REC_OPTOUT_ON = 1;
		}

	}

}
