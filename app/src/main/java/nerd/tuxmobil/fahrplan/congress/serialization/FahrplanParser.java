package nerd.tuxmobil.fahrplan.congress.serialization;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values;
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.serialization.exceptions.MissingXmlAttributeException;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import nerd.tuxmobil.fahrplan.congress.utils.LectureUtils;
import nerd.tuxmobil.fahrplan.congress.validation.DateFieldValidation;

import static nerd.tuxmobil.fahrplan.congress.serialization.XmlPullParsers.getSanitizedText;

public class FahrplanParser {

    public interface OnParseCompleteListener {

        void onParseDone(Boolean result, String version);
    }

    private ParserTask task;

    private OnParseCompleteListener listener;

    private Context context;

    private AppRepository appRepository;

    public FahrplanParser(Context context, AppRepository appRepository) {
        task = null;
        MyApp.parser = this;
        this.context = context;
        this.appRepository = appRepository;
    }

    public void parse(String fahrplan, String eTag) {
        task = new ParserTask(context, appRepository, listener);
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

class ParserTask extends AsyncTask<String, Void, Boolean> {

    private String LOG_TAG = "ParseFahrplan";

    private List<Lecture> lectures;

    private Meta meta;

    private SQLiteDatabase db;

    private FahrplanParser.OnParseCompleteListener listener;

    private boolean completed;

    private boolean result;

    private Context context;

    private AppRepository appRepository;

    public ParserTask(Context context, AppRepository appRepository, FahrplanParser.OnParseCompleteListener listener) {
        this.listener = listener;
        this.completed = false;
        this.db = null;
        this.context = context;
        this.appRepository = appRepository;
    }

    public void setListener(FahrplanParser.OnParseCompleteListener listener) {
        this.listener = listener;

        if (completed && (listener != null)) {
            notifyActivity();
        }
    }

    @Override
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
        listener.onParseDone(result, meta.getVersion());
        completed = false;
    }

    protected void onPostExecute(Boolean result) {
        completed = true;
        this.result = result;

        if (listener != null) {
            notifyActivity();
        }
    }

    public void storeLectureList(Context context, List<Lecture> lectures) {
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
                values.put(Columns.SLUG, lecture.slug);
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
            HashMap<String, Integer> roomsMap = new HashMap<>();
            while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        lectures = new ArrayList<>();
                        meta = new Meta();
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
                            meta.setVersion(getSanitizedText(parser));
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
                                            lecture.title = getSanitizedText(parser);
                                        } else if (name.equals("subtitle")) {
                                            parser.next();
                                            lecture.subtitle = getSanitizedText(parser);
                                        } else if (name.equals("slug")) {
                                            parser.next();
                                            lecture.slug = getSanitizedText(parser);
                                        } else if (name.equals("track")) {
                                            parser.next();
                                            lecture.track = getSanitizedText(parser);
                                        } else if (name.equals("type")) {
                                            parser.next();
                                            lecture.type = getSanitizedText(parser);
                                        } else if (name.equals("language")) {
                                            parser.next();
                                            lecture.lang = getSanitizedText(parser);
                                        } else if (name.equals("abstract")) {
                                            parser.next();
                                            lecture.abstractt = getSanitizedText(parser);
                                        } else if (name.equals("description")) {
                                            parser.next();
                                            lecture.description = getSanitizedText(parser);
                                        } else if (name.equals("person")) {
                                            parser.next();
                                            String separator = lecture.speakers.length() > 0 ? ";" : "";
                                            lecture.speakers = lecture.speakers + separator + getSanitizedText(parser);
                                        } else if (name.equals("link")) {
                                            String url = parser.getAttributeValue(null, "href");
                                            parser.next();
                                            String urlName = getSanitizedText(parser);
                                            if (url == null) {
                                                url = urlName;
                                            }
                                            if (!url.contains("://")) {
                                                url = "http://" + url;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            if (lecture.links.length() > 0) {
                                                sb.append(lecture.links);
                                                sb.append(",");
                                            }
                                            sb.append("[").append(urlName).append("]").append("(")
                                                    .append(url).append(")");
                                            lecture.links = sb.toString();
                                        } else if (name.equals("start")) {
                                            parser.next();
                                            lecture.startTime = Lecture.parseStartTime(getSanitizedText(parser));
                                            lecture.relStartTime = lecture.startTime;
                                            if (lecture.relStartTime < dayChangeTime) {
                                                lecture.relStartTime += (24 * 60);
                                            }
                                        } else if (name.equals("duration")) {
                                            parser.next();
                                            lecture.duration = Lecture.parseDuration(getSanitizedText(parser));
                                        } else if (name.equals("date")) {
                                            parser.next();
                                            lecture.dateUTC = DateHelper.getDateTime(getSanitizedText(parser));
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
                                                            lecture.recordingLicense = getSanitizedText(parser);
                                                        } else if (name.equals("optout")) {
                                                            parser.next();
                                                            lecture.recordingOptOut = Boolean.valueOf(getSanitizedText(parser));
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
                                            meta.setSubtitle(getSanitizedText(parser));
                                        }
                                        if (name.equals("title")) {
                                            parser.next();
                                            meta.setTitle(getSanitizedText(parser));
                                        }
                                        if (name.equals("release")) {
                                            parser.next();
                                            meta.setVersion(getSanitizedText(parser));
                                        }
                                        if (name.equals("day_change")) {
                                            parser.next();
                                            dayChangeTime = Lecture.parseStartTime(getSanitizedText(parser));
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
            meta.setNumDays(numdays);
            meta.setETag(eTag);
            appRepository.updateMeta(meta);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setChangedFlags(List<Lecture> lectures) {
        List<Lecture> oldLectures;
        boolean changed = false;

        oldLectures = FahrplanMisc.loadLecturesForAllDays(this.context);
        if (oldLectures.isEmpty()) {
            return;
        }

        int lectureIndex = oldLectures.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = oldLectures.get(lectureIndex);
            if (l.changedIsCanceled) oldLectures.remove(lectureIndex);
            lectureIndex--;
        }

        lectureIndex = 0;
        for (lectureIndex = 0; lectureIndex < lectures.size(); lectureIndex++) {
            Lecture newLecture = lectures.get(lectureIndex);
            Lecture oldLecture = LectureUtils.getLecture(oldLectures, newLecture.lecture_id);

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
