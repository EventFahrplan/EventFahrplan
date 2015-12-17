package nerd.tuxmobil.fahrplan.congress;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Columns;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Values;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.MetasTable;

interface OnParseCompleteListener {

    public void onParseDone(Boolean result, String version);
}

public class FahrplanParser {

    private parser task;

    private OnParseCompleteListener listener;

    private Context context;

    public FahrplanParser(Context context) {
        task = null;
        MyApp.parser = this;
        this.context = context;
    }

    public void parse(String fahrplan, String eTag) {
        task = new parser(listener, context);
        task.execute(fahrplan, eTag);
    }

    public void cancel() {
        if (task != null) {
            task.cancel(false);
        }
    }

    public void setListener(OnParseCompleteListener listener) {
        this.listener = listener;
        if (task != null) {
            task.setListener(listener);
        }
    }
}

class parser extends AsyncTask<String, Void, Boolean> {

    private String LOG_TAG = "ParseFahrplan";

    private LectureList lectures;

    private MetaInfo meta;

    private MetaDBOpenHelper metaDB;

    private SQLiteDatabase db;

    private OnParseCompleteListener listener;

    private boolean completed;

    private boolean result;

    private Context context;

    public parser(OnParseCompleteListener listener, Context context) {
        this.listener = listener;
        this.completed = false;
        this.db = null;
        this.context = context;
    }

    public void setListener(OnParseCompleteListener listener) {
        this.listener = listener;

        if (completed && (listener != null)) {
            notifyActivity();
        }
    }

    protected Boolean doInBackground(String... args) {
        boolean parsingSuccessful = parseFahrplan(args[0], args[1]);
        if (parsingSuccessful) {
            DateFieldValidation dateFieldValidation = new DateFieldValidation(context);
            dateFieldValidation.validate();
            dateFieldValidation.printValidationErrors();
            // TODO Clear database on validation failure.
        }
        return parsingSuccessful;
    }

    protected void onCancelled() {
        MyApp.LogDebug(LOG_TAG, "parse cancelled");
        if (db != null) {
            db.close();
        }
    }

    private void notifyActivity() {
        listener.onParseDone(result, meta.version);
        completed = false;
    }

    protected void onPostExecute(Boolean result) {
        completed = true;
        this.result = result;

        if (listener != null) {
            notifyActivity();
        }
    }

    public void storeMeta(Context context, MetaInfo meta) {
        MyApp.LogDebug(LOG_TAG, "storeMeta");
        metaDB = new MetaDBOpenHelper(context);

        db = metaDB.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            db.beginTransaction();
            db.delete(MetasTable.NAME, null, null);
            values.put(MetasTable.Columns.NUM_DAYS, meta.numdays);
            values.put(MetasTable.Columns.VERSION, meta.version);
            values.put(MetasTable.Columns.TITLE, meta.title);
            values.put(MetasTable.Columns.SUBTITLE, meta.subtitle);
            values.put(MetasTable.Columns.DAY_CHANGE_HOUR, meta.dayChangeHour);
            values.put(MetasTable.Columns.DAY_CHANGE_MINUTE, meta.dayChangeMinute);
            values.put(MetasTable.Columns.ETAG, meta.eTag);

            db.insert(MetasTable.NAME, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void storeLectureList(Context context, ArrayList<Lecture> lectures) {
        MyApp.LogDebug(LOG_TAG, "storeLectureList");
        LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

        db = lecturesDB.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(LecturesTable.NAME, null, null);
            for (Lecture lecture : lectures) {
                if (isCancelled()) {
                    break;
                }
                ContentValues values = new ContentValues();
                values.put(Columns.EVENT_ID, lecture.lecture_id);
                values.put(Columns.TITLE, lecture.title);
                values.put(Columns.SUBTITLE, lecture.subtitle);
                values.put(Columns.DAY, lecture.day);
                values.put(Columns.ROOM, lecture.room);
                values.put(Columns.START, lecture.startTime);
                values.put(Columns.DURATION, lecture.duration);
                values.put(Columns.SPEAKERS, lecture.speakers);
                values.put(Columns.TRACK, lecture.track);
                values.put(Columns.TYPE, lecture.type);
                values.put(Columns.LANG, lecture.lang);
                values.put(Columns.ABSTRACT, lecture.abstractt);
                values.put(Columns.DESCR, lecture.description);
                values.put(Columns.LINKS, lecture.links);
                values.put(Columns.REL_START, lecture.relStartTime);
                values.put(Columns.DATE, lecture.date);
                values.put(Columns.DATE_UTC, lecture.dateUTC);
                values.put(Columns.ROOM_IDX, lecture.room_index);
                values.put(Columns.REC_LICENSE, lecture.recordingLicense);
                values.put(Columns.REC_OPTOUT,
                        lecture.recordingOptOut ? Values.REC_OPTOUT_ON : Values.REC_OPTOUT_OFF);
                values.put(Columns.CHANGED_TITLE, lecture.changedTitle);
                values.put(Columns.CHANGED_SUBTITLE, lecture.changedSubtitle);
                values.put(Columns.CHANGED_ROOM, lecture.changedRoom);
                values.put(Columns.CHANGED_DAY, lecture.changedDay);
                values.put(Columns.CHANGED_SPEAKERS, lecture.changedSpeakers);
                values.put(Columns.CHANGED_RECORDING_OPTOUT, lecture.changedRecordingOptOut);
                values.put(Columns.CHANGED_LANGUAGE, lecture.changedLanguage);
                values.put(Columns.CHANGED_TRACK, lecture.changedTrack);
                values.put(Columns.CHANGED_IS_NEW, lecture.changedIsNew);
                values.put(Columns.CHANGED_TIME, lecture.changedTime);
                values.put(Columns.CHANGED_DURATION, lecture.changedDuration);
                values.put(Columns.CHANGED_IS_CANCELED, lecture.changedIsCanceled);
                db.insert(LecturesTable.NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private Boolean parseFahrplan(String fahrplan, String eTag) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(fahrplan));
            int eventType = parser.getEventType();
            boolean done = false;
            int numdays = 0;
            String room = null;
            int day = 0;
            int dayChangeTime = 600; // hardcoded as not provided
            String date = "";
            int room_index = 0;
            int room_map_index = 0;
            boolean schedule_complete = false;
            HashMap<String, Integer> roomsMap = new HashMap<String, Integer>();
            while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        lectures = new LectureList();
                        meta = new MetaInfo();
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equals("schedule")) {
                            schedule_complete = true;
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals("version")) {
                            parser.next();
                            meta.version = new String(parser.getText());
                        }
                        if (name.equals("day")) {
                            String index = parser.getAttributeValue(null, "index");
                            day = Integer.parseInt(index);
                            date = parser.getAttributeValue(null, "date");
                            String end = parser.getAttributeValue(null, "end");
                            if (end == null) {
                                MyApp.LogDebug(LOG_TAG,
                                        "Current day: date = " + date + ", index = " + day);
                                throw new MissingXmlAttributeException("day", "end");
                            }
                            dayChangeTime = DateHelper.getDayChange(end);
                            if (day > numdays) {
                                numdays = day;
                            }
                        }
                        if (name.equals("room")) {
                            room = new String(parser.getAttributeValue(null, "name"));
                            if (!roomsMap.containsKey(room)) {
                                roomsMap.put(room, room_index);
                                room_map_index = room_index;
                                room_index++;
                            } else {
                                room_map_index = roomsMap.get(room);
                            }
                        }
                        if (name.equalsIgnoreCase("event")) {
                            String id = parser.getAttributeValue(null, "id");
                            Lecture lecture = new Lecture(id);
                            lecture.day = day;
                            lecture.room = room;
                            lecture.date = date;
                            lecture.room_index = room_map_index;
                            MyApp.LogDebug(LOG_TAG,
                                    "room " + room + " with index " + room_map_index);
                            eventType = parser.next();
                            boolean lecture_done = false;
                            while (eventType != XmlPullParser.END_DOCUMENT
                                    && !lecture_done && !isCancelled()) {
                                switch (eventType) {
                                    case XmlPullParser.END_TAG:
                                        name = parser.getName();
                                        if (name.equals("event")) {
                                            lectures.add(lecture);
                                            lecture_done = true;
                                        }
                                        break;
                                    case XmlPullParser.START_TAG:
                                        name = parser.getName();
                                        if (name.equals("title")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.title = parser.getText();
                                            }
                                        } else if (name.equals("subtitle")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.subtitle = parser.getText();
                                            }
                                        } else if (name.equals("track")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.track = parser.getText();
                                            }
                                        } else if (name.equals("type")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.type = parser.getText();
                                            }
                                        } else if (name.equals("language")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.lang = parser.getText();
                                            }
                                        } else if (name.equals("abstract")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.abstractt = parser.getText();
                                            }
                                        } else if (name.equals("description")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.description = parser.getText();
                                            }
                                        } else if (name.equals("person")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.speakers = lecture.speakers + (
                                                        lecture.speakers.length() > 0 ? ";" : "")
                                                        + parser.getText();
                                            }
                                        } else if (name.equals("link")) {
                                            String url = parser.getAttributeValue(null, "href");
                                            parser.next();
                                            String urlname = parser.getText();
                                            if (url == null) {
                                                url = urlname;
                                            }
                                            if (!url.contains("://")) {
                                                url = "http://" + url;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            if (lecture.links.length() > 0) {
                                                sb.append(lecture.links);
                                                sb.append(",");
                                            }
                                            sb.append("[").append(urlname).append("]").append("(")
                                                    .append(url).append(")");
                                            lecture.links = sb.toString();
                                        } else if (name.equals("start")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.startTime = Lecture
                                                        .parseStartTime(parser.getText());
                                            }
                                            lecture.relStartTime = lecture.startTime;
                                            if (lecture.relStartTime < dayChangeTime) {
                                                lecture.relStartTime += (24 * 60);
                                            }
                                        } else if (name.equals("duration")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.duration = Lecture
                                                        .parseDuration(parser.getText());
                                            }
                                        } else if (name.equals("date")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                lecture.dateUTC = DateHelper
                                                        .getDateTime(parser.getText());
                                            }
                                        } else if (name.equals("recording")) {
                                            eventType = parser.next();
                                            boolean recording_done = false;
                                            while (eventType != XmlPullParser.END_DOCUMENT
                                                    && !recording_done && !isCancelled()) {
                                                switch (eventType) {
                                                    case XmlPullParser.END_TAG:
                                                        name = parser.getName();
                                                        if (name.equals("recording")) {
                                                            recording_done = true;
                                                        }
                                                        break;
                                                    case XmlPullParser.START_TAG:
                                                        name = parser.getName();
                                                        if (name.equals("license")) {
                                                            parser.next();
                                                            if (parser.getText() != null) {
                                                                lecture.recordingLicense = parser
                                                                        .getText();
                                                            }
                                                        } else if (name.equals("optout")) {
                                                            parser.next();
                                                            if (parser.getText() != null) {
                                                                lecture.recordingOptOut = Boolean
                                                                        .valueOf(parser.getText());
                                                            }
                                                        }
                                                        break;
                                                }
                                                if (recording_done) {
                                                    break;
                                                }
                                                eventType = parser.next();
                                            }
                                        }
                                        break;
                                }
                                if (lecture_done) {
                                    break;
                                }
                                eventType = parser.next();
                            }
                        } else if (name.equalsIgnoreCase("conference")) {
                            boolean conf_done = false;
                            eventType = parser.next();
                            while (eventType != XmlPullParser.END_DOCUMENT
                                    && !conf_done) {
                                switch (eventType) {
                                    case XmlPullParser.END_TAG:
                                        name = parser.getName();
                                        if (name.equals("conference")) {
                                            conf_done = true;
                                        }
                                        break;
                                    case XmlPullParser.START_TAG:
                                        name = parser.getName();
                                        if (name.equals("subtitle")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                meta.subtitle = new String(parser.getText());
                                            } else {
                                                meta.subtitle = "";
                                            }
                                        }
                                        if (name.equals("title")) {
                                            parser.next();
                                            meta.title = new String(parser.getText());
                                        }
                                        if (name.equals("release")) {
                                            parser.next();
                                            meta.version = new String(parser.getText());
                                        }
                                        if (name.equals("day_change")) {
                                            parser.next();
                                            if (parser.getText() != null) {
                                                dayChangeTime = Lecture
                                                        .parseStartTime(parser.getText());
                                            }
                                        }
                                        break;
                                }
                                if (conf_done) {
                                    break;
                                }
                                eventType = parser.next();
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
            if (!schedule_complete) return false;
            if (isCancelled()) {
                return false;
            }
            setChangedFlags(lectures);
            storeLectureList(context, lectures);
            if (isCancelled()) {
                return false;
            }
            meta.numdays = numdays;
            meta.eTag = eTag;
            storeMeta(context, meta);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setChangedFlags(LectureList lectures) {
        LectureList oldLectures;
        boolean changed = false;

        oldLectures = FahrplanMisc.loadLecturesForAllDays(this.context);
        if (oldLectures == null) return;

        int lectureIndex = oldLectures.size()-1;
        while (lectureIndex >= 0) {
            Lecture l = oldLectures.get(lectureIndex);
            if (l.changedIsCanceled) oldLectures.remove(lectureIndex);
            lectureIndex--;
        }

        lectureIndex = 0;
        for (lectureIndex = 0; lectureIndex < lectures.size(); lectureIndex++) {
            Lecture newLecture = lectures.get(lectureIndex);
            Lecture oldLecture = oldLectures.getLecture(newLecture.lecture_id);

            if (oldLecture == null) {
                newLecture.changedIsNew = true;
                MyApp.LogDebug(LOG_TAG, "lecture " + newLecture.title + " is new.");
                changed = true;
                continue;
            }

            if (oldLecture.equals(newLecture)) {
                oldLectures.remove(oldLecture);
                continue;
            }

            if (!(newLecture.title.equals(oldLecture.title))) {
                newLecture.changedTitle = true;
                MyApp.LogDebug(LOG_TAG, "title changed to " + newLecture.title);
                changed = true;
            }
            if (!(newLecture.subtitle.equals(oldLecture.subtitle))) {
                newLecture.changedSubtitle = true;
                MyApp.LogDebug(LOG_TAG, "subtitle changed to " + newLecture.subtitle);
                changed = true;
            }
            if (!(newLecture.speakers.equals(oldLecture.speakers))) {
                newLecture.changedSpeakers = true;
                MyApp.LogDebug(LOG_TAG, "speakers changed to " + newLecture.speakers);
                changed = true;
            }
            if (!(newLecture.lang.equals(oldLecture.lang))) {
                newLecture.changedLanguage = true;
                MyApp.LogDebug(LOG_TAG, "lang changed to " + newLecture.lang);
                changed = true;
            }
            if (!(newLecture.room.equals(oldLecture.room))) {
                newLecture.changedRoom = true;
                MyApp.LogDebug(LOG_TAG, "room changed to " + newLecture.room);
                changed = true;
            }
            if (!(newLecture.track.equals(oldLecture.track))) {
                newLecture.changedTrack = true;
                MyApp.LogDebug(LOG_TAG, "track changed to " + newLecture.track);
                changed = true;
            }
            if (newLecture.recordingOptOut != oldLecture.recordingOptOut) {
                newLecture.changedRecordingOptOut = true;
                MyApp.LogDebug(LOG_TAG, "recordingOptOut changed to " + newLecture.recordingOptOut);
                changed = true;
            }
            if (newLecture.day != oldLecture.day) {
                newLecture.changedDay = true;
                MyApp.LogDebug(LOG_TAG, "day changed to " + newLecture.day);
                changed = true;
            }
            if (newLecture.startTime != oldLecture.startTime) {
                newLecture.changedTime = true;
                MyApp.LogDebug(LOG_TAG, "startTime changed to " + newLecture.startTime);
                changed = true;
            }
            if (newLecture.duration != oldLecture.duration) {
                newLecture.changedDuration = true;
                MyApp.LogDebug(LOG_TAG, "duration changed to " + newLecture.duration);
                changed = true;
            }

            oldLectures.remove(oldLecture);
        }

        for (lectureIndex = 0; lectureIndex < oldLectures.size(); lectureIndex++) {
            Lecture oldLecture = oldLectures.get(lectureIndex);
            oldLecture.cancel();
            lectures.add(oldLecture);
            MyApp.LogDebug(LOG_TAG, "lecture " + oldLecture.title + " was canceled.");
            changed = true;
        }

        if (changed) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(BundleKeys.PREFS_CHANGES_SEEN, false);
            edit.commit();
        }
    }

}
