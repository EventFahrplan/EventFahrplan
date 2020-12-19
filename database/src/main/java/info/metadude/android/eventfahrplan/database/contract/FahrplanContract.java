package info.metadude.android.eventfahrplan.database.contract;

import android.provider.BaseColumns;

public interface FahrplanContract {

    interface MetasTable {

        String NAME = "meta";

        interface Columns {

            /* 0 */ String VERSION = "version";
            /* 1 */ String TITLE = "title";
            /* 2 */ String SUBTITLE = "subtitle";
            /* 3 */ // Zombie: Former "day_change_hour" column.
            /* 4 */ // Zombie: Former "day_change_minute" column.
            /* 5 */ String ETAG = "etag";
            /* 6 */ String NUM_DAYS = "numdays";
            /* 7 */ String TIME_ZONE_NAME = "time_zone_name";
        }

        interface Defaults {

            int NUM_DAYS_DEFAULT = 0;
            String ETAG_DEFAULT = "''";
        }

    }

    interface AlarmsTable {

        String NAME = "alarms";

        interface Columns {

            /* 0 */ String ID = "_id";
            /* 1 */ String SESSION_TITLE = "title";
            /* 2 */ String ALARM_TIME_IN_MIN = "alarm_time_in_min";
            /* 3 */ String TIME = "time";
            /* 4 */ String TIME_TEXT = "timeText";
            /* 5 */ String SESSION_ID = "eventid"; // Keep column name to avoid database migration.
            /* 6 */ String DISPLAY_TIME = "displayTime";
            /* 7 */ String DAY = "day";
        }

        interface Defaults {

            int DEFAULT_VALUE_ID = 0;
            int ALARM_TIME_IN_MIN_DEFAULT = -1;
        }

    }

    interface HighlightsTable {

        String NAME = "highlight";

        interface Columns {

            /* 0 */ String SESSION_ID = "eventid"; // Keep column name to avoid database migration.
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

        String NAME = "lectures"; // Keep table name to avoid database migration.

        interface Columns extends BaseColumns {

            @Deprecated // Value is unused. Query primary key _ID column instead.
            /* 00 */ String SESSION_ID = "event_id"; // Keep column name to avoid database migration.
            /* 01 */ String TITLE = "title";
            /* 02 */ String SUBTITLE = "subtitle";
            /* 03 */ String DAY = "day";
            /* 04 */ String ROOM = "room";
            /* 05 */ String START = "start";
            /* 06 */ String DURATION = "duration";
            /* 07 */ String SPEAKERS = "speakers";
            /* 08 */ String TRACK = "track";
            /* 09 */ String TYPE = "type";
            /* 10 */ String LANG = "lang";
            /* 11 */ String ABSTRACT = "abstract";
            /* 12 */ String DESCR = "descr";
            /* 13 */ String REL_START = "relStart";
            /* 14 */ String DATE = "date";
            /* 15 */ String LINKS = "links";
            /* 16 */ String DATE_UTC = "dateUTC";
            /* 17 */ String ROOM_IDX = "room_idx";
            /* 18 */ String REC_LICENSE = "rec_license";
            /* 19 */ String REC_OPTOUT = "rec_optout";
            /* 20 */ String CHANGED_TITLE = "changed_title";
            /* 21 */ String CHANGED_SUBTITLE = "changed_subtitle";
            /* 22 */ String CHANGED_ROOM = "changed_room";
            /* 23 */ String CHANGED_DAY = "changed_day";
            /* 24 */ String CHANGED_SPEAKERS = "changed_speakers";
            /* 25 */ String CHANGED_RECORDING_OPTOUT = "changed_recording_optout";
            /* 26 */ String CHANGED_LANGUAGE = "changed_language";
            /* 27 */ String CHANGED_TRACK = "changed_track";
            /* 28 */ String CHANGED_IS_NEW = "changed_is_new";
            /* 29 */ String CHANGED_TIME = "changed_time";
            /* 30 */ String CHANGED_DURATION = "changed_duration";
            /* 31 */ String CHANGED_IS_CANCELED = "changed_is_canceled";
            /* 32 */ String SLUG = "slug";
            /* 33 */ String URL = "url";
            /* 34 */ String TIME_ZONE_OFFSET = "time_zone_offset";
            /* 35 */ String GUID = "guid";
        }

        interface Defaults {

            int DATE_UTC_DEFAULT = 0;
            int ROOM_IDX_DEFAULT = 0;
        }

        interface Values {

            int REC_OPT_OUT_OFF = 0;
            int REC_OPT_OUT_ON = 1;
        }

    }

}
