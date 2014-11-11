package nerd.tuxmobil.fahrplan.congress;

import android.provider.BaseColumns;

public interface FahrplanContract {

    public interface FragmentTags {

        public static final String DETAIL = "detail";
        public static final String SCHEDULE = "schedule";
    }

    public interface MetasTable {

        public static final String NAME = "meta";

        public interface Columns {

            public static final String NUM_DAYS = "numdays";
            // 0
            public static final String VERSION = "version";
            // 1
            public static final String TITLE = "title";
            // 2
            public static final String SUBTITLE = "subtitle";
            // 3
            public static final String DAY_CHANGE_HOUR = "day_change_hour";                // 4
            public static final String DAY_CHANGE_MINUTE = "day_change_minute";        // 5
            public static final String ETAG = "etag";
            // 6
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
            // 0
            public static final String EVENT_TITLE = "title";
            // 1
            public static final String ALARM_TIME_IN_MIN = "alarm_time_in_min";        // 2
            public static final String TIME = "time";
            // 3
            public static final String TIME_TEXT = "timeText";
            // 4
            public static final String EVENT_ID = "eventid";
            // 5
            public static final String DISPLAY_TIME = "displayTime";
            // 6
            public static final String DAY = "day";
            // 7
        }

        public interface Defaults {

            public static final int ALARM_TIME_IN_MIN_DEFAULT = -1;
        }

    }

    public interface HighlightsTable {

        public static final String NAME = "highlight";

        public interface Columns {

            public static final String ID = "_id";                                // 0
            public static final String EVENT_ID = "eventid";                // 1
            public static final String HIGHLIGHT = "highlight";        // 2
        }

        public interface Values {

            public static final int HIGHLIGHT_STATE_OFF = 0;
            public static final int HIGHLIGHT_STATE_ON = 1;
        }

    }

    public interface LecturesTable {

        public static final String NAME = "lectures";

        public interface Columns extends BaseColumns {

            public static final String EVENT_ID = "event_id";                // 00
            public static final String TITLE = "title";                                // 01
            public static final String SUBTITLE = "subtitle";                        // 02
            public static final String DAY = "day";                                        // 03
            public static final String ROOM = "room";                                        // 04
            public static final String START = "start";                                // 05
            public static final String DURATION = "duration";                        // 06
            public static final String SPEAKERS = "speakers";                        // 07
            public static final String TRACK = "track";                                // 08
            public static final String TYPE = "type";                                        // 09
            public static final String LANG = "lang";                                        // 10
            public static final String ABSTRACT = "abstract";                        // 11
            public static final String DESCR = "descr";                                // 12
            public static final String REL_START = "relStart";                // 13
            public static final String DATE = "date";                                        // 14
            public static final String LINKS = "links";                                // 15
            public static final String DATE_UTC = "dateUTC";                        // 16
            public static final String ROOM_IDX = "room_idx";                        // 17
            public static final String REC_LICENSE = "rec_license";        // 18
            public static final String REC_OPTOUT = "rec_optout";                // 19
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
