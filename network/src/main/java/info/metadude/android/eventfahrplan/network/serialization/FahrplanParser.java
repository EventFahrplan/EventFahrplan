package info.metadude.android.eventfahrplan.network.serialization;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.network.models.Lecture;
import info.metadude.android.eventfahrplan.network.models.Meta;
import info.metadude.android.eventfahrplan.network.serialization.exceptions.MissingXmlAttributeException;
import info.metadude.android.eventfahrplan.network.temporal.DateParser;
import info.metadude.android.eventfahrplan.network.validation.DateFieldValidation;

public class FahrplanParser {

    public interface OnParseCompleteListener {

        void onUpdateLectures(@NonNull List<Lecture> lectures);

        void onUpdateMeta(@NonNull Meta meta);

        void onParseDone(Boolean result, String version);
    }

    private ParserTask task;

    private OnParseCompleteListener listener;

    public FahrplanParser() {
        task = null;
    }

    public void parse(String fahrplan, String eTag) {
        task = new ParserTask(listener);
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

    private List<Lecture> lectures;

    private Meta meta;

    private FahrplanParser.OnParseCompleteListener listener;

    private boolean completed;

    private boolean result;

    ParserTask(FahrplanParser.OnParseCompleteListener listener) {
        this.listener = listener;
        this.completed = false;
    }

    public void setListener(FahrplanParser.OnParseCompleteListener listener) {
        this.listener = listener;

        if (completed && listener != null) {
            notifyActivity();
        }
    }

    @Override
    protected Boolean doInBackground(String... args) {
        boolean parsingSuccessful = parseFahrplan(args[0], args[1]);
        if (parsingSuccessful) {
            DateFieldValidation dateFieldValidation = new DateFieldValidation(Logging.Companion.get());
            dateFieldValidation.validate(lectures);
            dateFieldValidation.printValidationErrors();
            // TODO Clear database on validation failure.
        }
        return parsingSuccessful;
    }

    private void notifyActivity() {
        if (result) {
            listener.onUpdateLectures(lectures);
            listener.onUpdateMeta(meta);
        }
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

    private Boolean parseFahrplan(String fahrplan, String eTag) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(fahrplan));
            int eventType = parser.getEventType();
            boolean done = false;
            int numdays = 0;
            String room = null;
            int day = 0;
            int dayChangeTime = 600; // Only provided by Pentabarf; corresponds to 10:00 am.
            String date = "";
            int roomIndex = 0;
            int roomMapIndex = 0;
            boolean scheduleComplete = false;
            HashMap<String, Integer> roomsMap = new HashMap<>();
            while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        lectures = new ArrayList<>();
                        meta = new Meta();
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equals("schedule")) {
                            scheduleComplete = true;
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals("version")) {
                            parser.next();
                            meta.setVersion(XmlPullParsers.getSanitizedText(parser));
                        }
                        if (name.equals("day")) {
                            String index = parser.getAttributeValue(null, "index");
                            day = Integer.parseInt(index);
                            date = parser.getAttributeValue(null, "date");
                            String end = parser.getAttributeValue(null, "end");
                            if (end == null) {
                                throw new MissingXmlAttributeException("day", "end");
                            }
                            dayChangeTime = DateParser.Companion.getDayChange(end);
                            if (day > numdays) {
                                numdays = day;
                            }
                        }
                        if (name.equals("room")) {
                            room = parser.getAttributeValue(null, "name");
                            if (!roomsMap.containsKey(room)) {
                                roomsMap.put(room, roomIndex);
                                roomMapIndex = roomIndex;
                                roomIndex++;
                            } else {
                                roomMapIndex = roomsMap.get(room);
                            }
                        }
                        if (name.equalsIgnoreCase("event")) {
                            String id = parser.getAttributeValue(null, "id");
                            Lecture lecture = new Lecture();
                            lecture.setEventId(id);
                            lecture.setDayIndex(day);
                            lecture.setRoom(room);
                            lecture.setDate(date);
                            lecture.setRoomIndex(roomMapIndex);
                            eventType = parser.next();
                            boolean lectureDone = false;
                            while (eventType != XmlPullParser.END_DOCUMENT
                                    && !lectureDone && !isCancelled()) {
                                switch (eventType) {
                                    case XmlPullParser.END_TAG:
                                        name = parser.getName();
                                        if (name.equals("event")) {
                                            lectures.add(lecture);
                                            lectureDone = true;
                                        }
                                        break;
                                    case XmlPullParser.START_TAG:
                                        name = parser.getName();
                                        if (name.equals("title")) {
                                            parser.next();
                                            lecture.setTitle(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("subtitle")) {
                                            parser.next();
                                            lecture.setSubtitle(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("slug")) {
                                            parser.next();
                                            lecture.setSlug(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("url")) {
                                            parser.next();
                                            lecture.setUrl(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("track")) {
                                            parser.next();
                                            lecture.setTrack(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("type")) {
                                            parser.next();
                                            lecture.setType(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("language")) {
                                            parser.next();
                                            lecture.setLanguage(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("abstract")) {
                                            parser.next();
                                            lecture.setAbstractt(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("description")) {
                                            parser.next();
                                            lecture.setDescription(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("person")) {
                                            parser.next();
                                            String separator = lecture.getSpeakers().length() > 0 ? ";" : "";
                                            lecture.setSpeakers(lecture.getSpeakers() + separator + XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("link")) {
                                            String url = parser.getAttributeValue(null, "href");
                                            parser.next();
                                            String urlName = XmlPullParsers.getSanitizedText(parser);
                                            if (url == null) {
                                                url = urlName;
                                            }
                                            if (!url.contains("://")) {
                                                url = "http://" + url;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            if (lecture.getLinks().length() > 0) {
                                                sb.append(lecture.getLinks());
                                                sb.append(",");
                                            }
                                            sb.append("[").append(urlName).append("]").append("(")
                                                    .append(url).append(")");
                                            lecture.setLinks(sb.toString());
                                        } else if (name.equals("start")) {
                                            parser.next();
                                            lecture.setStartTime(Lecture.Companion.parseStartTime(XmlPullParsers.getSanitizedText(parser)));
                                            lecture.setRelativeStartTime(lecture.getStartTime());
                                            if (lecture.getRelativeStartTime() < dayChangeTime) {
                                                lecture.setRelativeStartTime(lecture.getRelativeStartTime() + 24 * 60);
                                            }
                                        } else if (name.equals("duration")) {
                                            parser.next();
                                            lecture.setDuration(Lecture.Companion.parseDuration(XmlPullParsers.getSanitizedText(parser)));
                                        } else if (name.equals("date")) {
                                            parser.next();
                                            lecture.setDateUTC(DateParser.Companion.getDateTime(XmlPullParsers.getSanitizedText(parser)));
                                        } else if (name.equals("recording")) {
                                            eventType = parser.next();
                                            boolean recordingDone = false;
                                            while (eventType != XmlPullParser.END_DOCUMENT
                                                    && !recordingDone && !isCancelled()) {
                                                switch (eventType) {
                                                    case XmlPullParser.END_TAG:
                                                        name = parser.getName();
                                                        if (name.equals("recording")) {
                                                            recordingDone = true;
                                                        }
                                                        break;
                                                    case XmlPullParser.START_TAG:
                                                        name = parser.getName();
                                                        if (name.equals("license")) {
                                                            parser.next();
                                                            lecture.setRecordingLicense(XmlPullParsers.getSanitizedText(parser));
                                                        } else if (name.equals("optout")) {
                                                            parser.next();
                                                            lecture.setRecordingOptOut(Boolean.valueOf(XmlPullParsers.getSanitizedText(parser)));
                                                        }
                                                        break;
                                                }
                                                if (recordingDone) {
                                                    break;
                                                }
                                                eventType = parser.next();
                                            }
                                        }
                                        break;
                                }
                                if (lectureDone) {
                                    break;
                                }
                                eventType = parser.next();
                            }
                        } else if (name.equalsIgnoreCase("conference")) {
                            boolean confDone = false;
                            eventType = parser.next();
                            while (eventType != XmlPullParser.END_DOCUMENT
                                    && !confDone) {
                                switch (eventType) {
                                    case XmlPullParser.END_TAG:
                                        name = parser.getName();
                                        if (name.equals("conference")) {
                                            confDone = true;
                                        }
                                        break;
                                    case XmlPullParser.START_TAG:
                                        name = parser.getName();
                                        if (name.equals("subtitle")) {
                                            parser.next();
                                            meta.setSubtitle(XmlPullParsers.getSanitizedText(parser));
                                        }
                                        if (name.equals("title")) {
                                            parser.next();
                                            meta.setTitle(XmlPullParsers.getSanitizedText(parser));
                                        }
                                        if (name.equals("release")) {
                                            parser.next();
                                            meta.setVersion(XmlPullParsers.getSanitizedText(parser));
                                        }
                                        if (name.equals("day_change")) {
                                            parser.next();
                                            dayChangeTime = Lecture.Companion.parseStartTime(XmlPullParsers.getSanitizedText(parser));
                                        }
                                        break;
                                }
                                if (confDone) {
                                    break;
                                }
                                eventType = parser.next();
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
            if (!scheduleComplete) {
                return false;
            }
            if (isCancelled()) {
                return false;
            }
            meta.setNumDays(numdays);
            meta.setETag(eTag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
