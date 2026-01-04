package info.metadude.android.eventfahrplan.database.contract;

import android.provider.BaseColumns;

public interface FahrplanContract {

    interface MetasTable {

        String NAME = "meta";

        interface Columns {

            String NUM_DAYS = "numdays";
            String VERSION = "version";
            String TITLE = "title";
            String SUBTITLE = "subtitle";
            String SCHEDULE_ETAG = "etag";
            String TIME_ZONE_NAME = "time_zone_name";
            String SCHEDULE_LAST_MODIFIED = "schedule_last_modified";
            String SCHEDULE_GENERATOR_NAME = "schedule_generator_name";
            String SCHEDULE_GENERATOR_VERSION = "schedule_generator_version";
        }

        interface Defaults {

            int NUM_DAYS_DEFAULT = 0;
            String ETAG_DEFAULT = "";
        }

    }

    interface AlarmsTable {

        String NAME = "alarms";

        interface Columns {

            String ID = "_id";
            String SESSION_TITLE = "title";
            String TIME = "time";
            String SESSION_ID = "eventid"; // Keep column name to avoid database migration.
            String DAY_INDEX = "day";
        }

        interface Defaults {

            int DEFAULT_VALUE_ID = 0;
        }

    }

    interface HighlightsTable {

        String NAME = "highlight";

        interface Columns {

            String ID = "_id";
            String SESSION_ID = "eventid"; // Keep column name to avoid database migration.
            String HIGHLIGHT = "highlight";
        }

        interface Values {

            int HIGHLIGHT_STATE_OFF = 0;
            int HIGHLIGHT_STATE_ON = 1;
        }

    }

    interface SessionByNotificationIdTable {

        String NAME = "session_by_notification_id";

        interface Columns extends BaseColumns {

            String SESSION_ID = "session_id";

        }

    }

    interface SessionsTable {

        String NAME = "lectures"; // Keep table name to avoid database migration.

        interface Columns extends BaseColumns {

            String SESSION_ID = "event_id"; // Keep column name to avoid database migration.
            String TITLE = "title";
            String SUBTITLE = "subtitle";
            String DAY_INDEX = "day";
            String ROOM_NAME = "room";
            String ROOM_IDENTIFIER = "room_identifier";
            String SLUG = "slug";
            String START = "start";
            String DURATION = "duration";
            String FEEDBACK_URL = "feedback_url";
            String SPEAKERS = "speakers";
            String TRACK = "track";
            String TYPE = "type";
            String LANG = "lang";
            String ABSTRACT = "abstract";
            String DESCR = "descr";
            String REL_START = "relStart";
            String DATE_TEXT = "date";
            String LINKS = "links";
            String DATE_UTC = "dateUTC";
            String TIME_ZONE_OFFSET = "time_zone_offset";
            String ROOM_INDEX = "room_idx";
            String REC_LICENSE = "rec_license";
            String REC_OPTOUT = "rec_optout";
            String URL = "url";
            String CHANGED_TITLE = "changed_title";
            String CHANGED_SUBTITLE = "changed_subtitle";
            String CHANGED_ROOM_NAME = "changed_room";
            String CHANGED_DAY_INDEX = "changed_day";
            String CHANGED_SPEAKERS = "changed_speakers";
            String CHANGED_RECORDING_OPTOUT = "changed_recording_optout";
            String CHANGED_LANGUAGE = "changed_language";
            String CHANGED_TRACK = "changed_track";
            String CHANGED_IS_NEW = "changed_is_new";
            String CHANGED_START_TIME = "changed_time";
            String CHANGED_DURATION = "changed_duration";
            String CHANGED_IS_CANCELED = "changed_is_canceled";
            String SESSION_GUID = "event_guid"; // Keep column name to avoid database migration.
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
            String TITLE_NONE = "title_none_count";
            String TITLE_PRESENT = "title_present_count";

            String SUBTITLE_NONE = "subtitle_none_count";
            String SUBTITLE_PRESENT = "subtitle_present_count";

            String DAY_INDEX_NONE = "day_index_none_count";
            String DAY_INDEX_PRESENT = "day_index_present_count";

            String ROOM_NAME_NONE = "room_name_none_count";
            String ROOM_NAME_PRESENT = "room_name_present_count";

            String START_TIME_NONE = "start_time_none_count";
            String START_TIME_PRESENT = "start_time_present_count";

            String DURATION_NONE = "duration_none_count";
            String DURATION_PRESENT = "duration_present_count";

            String SPEAKERS_NONE = "speakers_none_count";
            String SPEAKERS_PRESENT = "speakers_present_count";

            String TRACK_NONE = "track_none_count";
            String TRACK_PRESENT = "track_present_count";

            String TYPE_NONE = "type_none_count";
            String TYPE_PRESENT = "type_present_count";

            String LANGUAGES_NONE = "languages_none_count";
            String LANGUAGES_PRESENT = "languages_present_count";

            String ABSTRACT_NONE = "abstract_none_count";
            String ABSTRACT_PRESENT = "abstract_present_count";

            String DESCRIPTION_NONE = "description_none_count";
            String DESCRIPTION_PRESENT = "description_present_count";

            String RELATIVE_START_TIME_NONE = "relative_start_time_none_count";
            String RELATIVE_START_TIME_PRESENT = "relative_start_time_present_count";

            String DATE_TEXT_NONE = "date_text_none_count";
            String DATE_TEXT_PRESENT = "date_text_present_count";

            String LINKS_NONE = "links_none_count";
            String LINKS_PRESENT = "links_present_count";

            String DATE_UTC_NONE = "date_utc_none_count";
            String DATE_UTC_PRESENT = "date_utc_present_count";

            String ROOM_INDEX_NONE = "room_index_none_count";
            String ROOM_INDEX_PRESENT = "room_index_present_count";

            String RECORDING_LICENSE_NONE = "recording_license_none_count";
            String RECORDING_LICENSE_PRESENT = "recording_license_present_count";

            String RECORDING_OPTOUT_NONE = "recording_optout_none_count";
            String RECORDING_OPTOUT_PRESENT = "recording_optout_present_count";

            String SLUG_NONE = "slug_none_count";
            String SLUG_PRESENT = "slug_present_count";

            String URL_NONE = "url_none_count";
            String URL_PRESENT = "url_present_count";

            String TIME_ZONE_OFFSET_NONE = "time_zone_offset_none_count";
            String TIME_ZONE_OFFSET_PRESENT = "time_zone_offset_present_count";

            String ROOM_IDENTIFIER_NONE = "room_identifier_none_count";
            String ROOM_IDENTIFIER_PRESENT = "room_identifier_present_count";

            String FEEDBACK_URL_NONE = "feedback_url_none_count";
            String FEEDBACK_URL_PRESENT = "feedback_url_present_count";
        }
    }

}
