package info.metadude.android.eventfahrplan.database.contract;

import android.provider.BaseColumns;

public interface FahrplanContract {

    interface MetasTable {

        String NAME = "meta";

        interface Columns {

            /* 0 */ String VERSION = "version";
            /* 1 */ String TITLE = "title";
            /* 2 */ String SUBTITLE = "subtitle";
            /* 3 */ String ETAG = "etag";
            /* 4 */ String NUM_DAYS = "num_days";
            /* 5 */ String TIME_ZONE_NAME = "time_zone_name";
        }

        interface Defaults {

            int NUM_DAYS_DEFAULT = 0;
        }

    }

    interface AlarmsTable {

        String NAME = "alarms";

        interface Columns {

            /* 0 */ String ID = "_id";
            /* 1 */ String SESSION_TITLE = "title";
            /* 2 */ String ALARM_TIME_IN_MIN = "alarm_time_in_min";
            /* 3 */ String TIME = "time";
            /* 4 */ String TIME_TEXT = "time_text";
            /* 5 */ String SESSION_ID = "session_id";
            /* 6 */ String DISPLAY_TIME = "display_time";
            /* 7 */ String DAY = "day";
        }

        interface Defaults {

            int DEFAULT_VALUE_ID = 0;
            int ALARM_TIME_IN_MIN_DEFAULT = -1;
        }

    }

    interface HighlightsTable {

        String NAME = "highlights";

        interface Columns {

            /* 0 */ String SESSION_ID = "session_id";
            /* 1 */ String HIGHLIGHT = "highlight";
            /* 2 */ String ID = "_id";
        }

        interface Values {

            int HIGHLIGHT_STATE_OFF = 0;
            int HIGHLIGHT_STATE_ON = 1;
        }

    }

    interface SessionByNotificationIdTable {

        String NAME = "session_by_notification_id";

        interface Columns extends BaseColumns {

            /* 00 */ String SESSION_ID = "session_id";

        }

    }

    interface SessionsTable {

        String NAME = "sessions";

        interface Columns extends BaseColumns {

            @Deprecated // Value is unused. Query primary key _ID column instead.
            /* 00 */ String SESSION_ID = "session_id";
            /* 01 */ String GUID = "guid";
            /* 02 */ String TITLE = "title";
            /* 03 */ String SUBTITLE = "subtitle";
            /* 04 */ String DAY = "day_index";
            /* 05 */ String ROOM = "room";
            /* 06 */ String START = "start";
            /* 07 */ String DURATION = "duration";
            /* 08 */ String SPEAKERS = "speakers";
            /* 09 */ String TRACK = "track";
            /* 10 */ String TYPE = "type";
            /* 11 */ String LANG = "language";
            /* 12 */ String ABSTRACT = "abstract";
            /* 13 */ String DESCR = "description";
            /* 14 */ String REL_START = "relative_start";
            /* 15 */ String DATE = "date";
            /* 16 */ String DATE_UTC = "date_utc";
            /* 17 */ String TIME_ZONE_OFFSET = "time_zone_offset";
            /* 18 */ String LINKS = "links";
            /* 19 */ String ROOM_IDX = "room_idx";
            /* 20 */ String REC_LICENSE = "rec_license";
            /* 21 */ String REC_OPTOUT = "rec_optout";
            /* 22 */ String SLUG = "slug";
            /* 23 */ String URL = "url";
            /* 24 */ String CHANGED_TITLE = "changed_title";
            /* 25 */ String CHANGED_SUBTITLE = "changed_subtitle";
            /* 26 */ String CHANGED_ROOM = "changed_room";
            /* 27 */ String CHANGED_DAY = "changed_day";
            /* 28 */ String CHANGED_SPEAKERS = "changed_speakers";
            /* 29 */ String CHANGED_RECORDING_OPTOUT = "changed_recording_optout";
            /* 30 */ String CHANGED_LANGUAGE = "changed_language";
            /* 31 */ String CHANGED_TRACK = "changed_track";
            /* 32 */ String CHANGED_IS_NEW = "changed_is_new";
            /* 33 */ String CHANGED_TIME = "changed_time";
            /* 34 */ String CHANGED_DURATION = "changed_duration";
            /* 35 */ String CHANGED_IS_CANCELED = "changed_is_canceled";
        }

        interface Values {

            int REC_OPT_OUT_OFF = 0;
            int REC_OPT_OUT_ON = 1;
        }

    }

}
