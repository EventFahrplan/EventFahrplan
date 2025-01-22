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
            /* 8 */ String SCHEDULE_LAST_MODIFIED = "schedule_last_modified";
        }

        interface Defaults {

            int NUM_DAYS_DEFAULT = 0;
            String ETAG_DEFAULT = "";
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

            /* 00 */ String SESSION_ID = "event_id"; // Keep column name to avoid database migration.
            /* 01 */ String TITLE = "title";
            /* 02 */ String SUBTITLE = "subtitle";
            /* 03 */ String DAY = "day";
            /* 04 */ String ROOM_NAME = "room";
            /* 05 */ String START = "start";
            /* 06 */ String DURATION = "duration";
            /* 07 */ String SPEAKERS = "speakers";
            /* 08 */ String TRACK = "track";
            /* 09 */ String TYPE = "type";
            /* 10 */ String LANG = "lang";
            /* 11 */ String ABSTRACT = "abstract";
            /* 12 */ String DESCR = "descr";
            /* 13 */ String REL_START = "relStart";
            /* 14 */ String DATE_TEXT = "date";
            /* 15 */ String LINKS = "links";
            /* 16 */ String DATE_UTC = "dateUTC";
            /* 17 */ String ROOM_INDEX = "room_idx";
            /* 18 */ String REC_LICENSE = "rec_license";
            /* 19 */ String REC_OPTOUT = "rec_optout";
            /* 20 */ String CHANGED_TITLE = "changed_title";
            /* 21 */ String CHANGED_SUBTITLE = "changed_subtitle";
            /* 22 */ String CHANGED_ROOM_NAME = "changed_room";
            /* 23 */ String CHANGED_DAY_INDEX = "changed_day";
            /* 24 */ String CHANGED_SPEAKERS = "changed_speakers";
            /* 25 */ String CHANGED_RECORDING_OPTOUT = "changed_recording_optout";
            /* 26 */ String CHANGED_LANGUAGE = "changed_language";
            /* 27 */ String CHANGED_TRACK = "changed_track";
            /* 28 */ String CHANGED_IS_NEW = "changed_is_new";
            /* 29 */ String CHANGED_START_TIME = "changed_time";
            /* 30 */ String CHANGED_DURATION = "changed_duration";
            /* 31 */ String CHANGED_IS_CANCELED = "changed_is_canceled";
            /* 32 */ String SLUG = "slug";
            /* 33 */ String URL = "url";
            /* 34 */ String TIME_ZONE_OFFSET = "time_zone_offset";
            /* 35 */ String ROOM_IDENTIFIER = "room_identifier";
            /* 36 */ String FEEDBACK_URL = "feedback_url";
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

    interface StatisticsView {

        String NAME = "sessions_column_stats";

        interface Columns extends BaseColumns {
            /* 00 */ String TITLE_NONE = "title_none_count";
            /* 01 */ String TITLE_PRESENT = "title_present_count";

            /* 02 */ String SUBTITLE_NONE = "subtitle_none_count";
            /* 03 */ String SUBTITLE_PRESENT = "subtitle_present_count";

            /* 04 */ String DAY_INDEX_NONE = "day_index_none_count";
            /* 05 */ String DAY_INDEX_PRESENT = "day_index_present_count";

            /* 06 */ String ROOM_NAME_NONE = "room_name_none_count";
            /* 07 */ String ROOM_NAME_PRESENT = "room_name_present_count";

            /* 08 */ String START_TIME_NONE = "start_time_none_count";
            /* 09 */ String START_TIME_PRESENT = "start_time_present_count";

            /* 10 */ String DURATION_NONE = "duration_none_count";
            /* 11 */ String DURATION_PRESENT = "duration_present_count";

            /* 12 */ String SPEAKERS_NONE = "speakers_none_count";
            /* 13 */ String SPEAKERS_PRESENT = "speakers_present_count";

            /* 14 */ String TRACK_NONE = "track_none_count";
            /* 15 */ String TRACK_PRESENT = "track_present_count";

            /* 16 */ String TYPE_NONE = "type_none_count";
            /* 17 */ String TYPE_PRESENT = "type_present_count";

            /* 18 */ String LANGUAGES_NONE = "languages_none_count";
            /* 19 */ String LANGUAGES_PRESENT = "languages_present_count";

            /* 20 */ String ABSTRACT_NONE = "abstract_none_count";
            /* 21 */ String ABSTRACT_PRESENT = "abstract_present_count";

            /* 22 */ String DESCRIPTION_NONE = "description_none_count";
            /* 23 */ String DESCRIPTION_PRESENT = "description_present_count";

            /* 24 */ String RELATIVE_START_TIME_NONE = "relative_start_time_none_count";
            /* 25 */ String RELATIVE_START_TIME_PRESENT = "relative_start_time_present_count";

            /* 26 */ String DATE_TEXT_NONE = "date_text_none_count";
            /* 27 */ String DATE_TEXT_PRESENT = "date_text_present_count";

            /* 28 */ String LINKS_NONE = "links_none_count";
            /* 29 */ String LINKS_PRESENT = "links_present_count";

            /* 30 */ String DATE_UTC_NONE = "date_utc_none_count";
            /* 31 */ String DATE_UTC_PRESENT = "date_utc_present_count";

            /* 32 */ String ROOM_INDEX_NONE = "room_index_none_count";
            /* 33 */ String ROOM_INDEX_PRESENT = "room_index_present_count";

            /* 34 */ String RECORDING_LICENSE_NONE = "recording_license_none_count";
            /* 35 */ String RECORDING_LICENSE_PRESENT = "recording_license_present_count";

            /* 36 */ String RECORDING_OPTOUT_NONE = "recording_optout_none_count";
            /* 37 */ String RECORDING_OPTOUT_PRESENT = "recording_optout_present_count";

            /* 38 */ String SLUG_NONE = "slug_none_count";
            /* 39 */ String SLUG_PRESENT = "slug_present_count";

            /* 40 */ String URL_NONE = "url_none_count";
            /* 41 */ String URL_PRESENT = "url_present_count";

            /* 42 */ String TIME_ZONE_OFFSET_NONE = "time_zone_offset_none_count";
            /* 43 */ String TIME_ZONE_OFFSET_PRESENT = "time_zone_offset_present_count";

            /* 44 */ String ROOM_IDENTIFIER_NONE = "room_identifier_none_count";
            /* 45 */ String ROOM_IDENTIFIER_PRESENT = "room_identifier_present_count";

            /* 46 */ String FEEDBACK_URL_NONE = "feedback_url_none_count";
            /* 47 */ String FEEDBACK_URL_PRESENT = "feedback_url_present_count";
        }
    }

}
