package info.metadude.android.eventfahrplan.database.contract;

import android.provider.BaseColumns;

public interface FahrplanContract {

    interface MetasTable {

        String NAME = "meta";

        interface Columns {

            /* 0 */ String VERSION = "version";
            /* 1 */ String TITLE = "title";
            /* 2 */ String SUBTITLE = "subtitle";
            /* 3 */ String DAY_CHANGE_HOUR = "day_change_hour";
            /* 4 */ String DAY_CHANGE_MINUTE = "day_change_minute";
            /* 5 */ String ETAG = "etag";
            /* 6 */ String NUM_DAYS = "numdays";
        }

        interface Defaults {

            int NUM_DAYS_DEFAULT = 0;
            int DAY_CHANGE_HOUR_DEFAULT = 4;
            int DAY_CHANGE_MINUTE_DEFAULT = 0;
            String ETAG_DEFAULT = "''";
        }

    }

    interface AlarmsTable {

        String NAME = "alarms";

        interface Columns {

            /* 0 */ String ID = "_id";
            /* 1 */ String EVENT_TITLE = "title";
            /* 2 */ String ALARM_TIME_IN_MIN = "alarm_time_in_min";
            /* 3 */ String TIME = "time";
            /* 4 */ String TIME_TEXT = "timeText";
            /* 5 */ String EVENT_ID = "eventid";
            /* 6 */ String DISPLAY_TIME = "displayTime";
            /* 7 */ String DAY = "day";
        }

        interface Defaults {

            int ALARM_TIME_IN_MIN_DEFAULT = -1;
        }

    }

    interface HighlightsTable {

        String NAME = "highlight";

        interface Columns {

            /* 0 */ String EVENT_ID = "eventid";
            /* 1 */ String HIGHLIGHT = "highlight";
            /* 2 */ String ID = "_id";
        }

        interface Values {

            int HIGHLIGHT_STATE_OFF = 0;
            int HIGHLIGHT_STATE_ON = 1;
        }

    }

    interface LecturesTable {

        String NAME = "lectures";

        interface Columns extends BaseColumns {

            /* 00 */ String EVENT_ID = "event_id";
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
        }

        interface Defaults {

            int DATE_UTC_DEFAULT = 0;
            int ROOM_IDX_DEFAULT = 0;
        }

        interface Values {

            int REC_OPTOUT_OFF = 0;
            int REC_OPTOUT_ON = 1;
        }

    }

}
