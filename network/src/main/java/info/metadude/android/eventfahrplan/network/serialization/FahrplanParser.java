package info.metadude.android.eventfahrplan.network.serialization;

import static info.metadude.android.eventfahrplan.commons.temporal.Moment.MINUTES_OF_ONE_DAY;

import android.os.AsyncTask;
import android.util.Xml;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.network.models.HttpHeader;
import info.metadude.android.eventfahrplan.network.models.Meta;
import info.metadude.android.eventfahrplan.network.models.Session;
import info.metadude.android.eventfahrplan.network.serialization.exceptions.MissingXmlAttributeException;
import info.metadude.android.eventfahrplan.network.temporal.DateParser;
import info.metadude.android.eventfahrplan.network.temporal.DurationParser;
import info.metadude.android.eventfahrplan.network.validation.DateFieldValidation;

public class FahrplanParser {

    public interface OnParseCompleteListener {

        void onUpdateSessions(@NonNull List<Session> sessions);

        void onUpdateMeta(@NonNull Meta meta);

        void onParseDone(Boolean isSuccess, String version);
    }

    @NonNull
    private final Logging logging;

    private ParserTask task;

    private OnParseCompleteListener listener;

    public FahrplanParser(@NonNull Logging logging) {
        this.logging = logging;
        task = null;
    }

    public void parse(String fahrplan, HttpHeader httpHeader) {
        task = new ParserTask(logging, listener);
        task.execute(fahrplan, httpHeader.getETag(), httpHeader.getLastModified());
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

    @NonNull
    private final Logging logging;

    private List<Session> sessions;

    private Meta meta;

    private FahrplanParser.OnParseCompleteListener listener;

    private boolean completed;

    private boolean isSuccess;

    ParserTask(@NonNull Logging logging, FahrplanParser.OnParseCompleteListener listener) {
        this.logging = logging;
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
        boolean parsingSuccessful = parseFahrplan(args[0], args[1], args[2]);
        if (parsingSuccessful) {
            DateFieldValidation dateFieldValidation = new DateFieldValidation(logging);
            dateFieldValidation.validate(sessions);
            dateFieldValidation.printValidationErrors();
            // TODO Clear database on validation failure.
        }
        return parsingSuccessful;
    }

    private void notifyActivity() {
        if (isSuccess) {
            listener.onUpdateMeta(meta);
            listener.onUpdateSessions(sessions);
        }
        listener.onParseDone(isSuccess, meta.getVersion());
        completed = false;
    }

    protected void onPostExecute(Boolean isSuccess) {
        completed = true;
        this.isSuccess = isSuccess;

        if (listener != null) {
            notifyActivity();
        }
    }

    private Boolean parseFahrplan(String fahrplan, String eTag, String lastModified) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(fahrplan));
            int eventType = parser.getEventType();
            boolean done = false;
            int numdays = 0;
            String roomName = null;
            String roomGuid = "";
            int day = 0;
            int dayChangeTime = 600; // Only provided by Pentabarf; corresponds to 10:00 am.
            String date = "";
            int roomIndex = 0;
            int roomMapIndex = 0;
            boolean scheduleComplete = false;
            Map<String, Integer> roomIndexByRoomName = new HashMap<>();
            while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        sessions = new ArrayList<>();
                        meta = new Meta();
                        meta.setHttpHeader(new HttpHeader(eTag, lastModified));
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
                            dayChangeTime = DateParser.getDayChange(end);
                            if (day > numdays) {
                                numdays = day;
                            }
                        }
                        if (name.equals("room")) {
                            roomName = parser.getAttributeValue(null, "name");
                            if (roomIndexByRoomName.containsKey(roomName)) {
                                roomMapIndex = roomIndexByRoomName.get(roomName);
                            } else {
                                roomIndexByRoomName.put(roomName, roomIndex);
                                roomMapIndex = roomIndex;
                                roomIndex++;
                            }
                            roomGuid = parser.getAttributeValue(null, "guid");
                        }
                        if (name.equalsIgnoreCase("event")) {
                            String id = parser.getAttributeValue(null, "id");
                            Session session = new Session();
                            session.setSessionId(id);
                            session.setDayIndex(day);
                            session.setRoomName(roomName);
                            session.setRoomGuid(Objects.requireNonNullElse(roomGuid, ""));
                            session.setDate(date);
                            session.setRoomIndex(roomMapIndex);
                            eventType = parser.next();
                            boolean isSessionDone = false;
                            while (eventType != XmlPullParser.END_DOCUMENT
                                    && !isSessionDone && !isCancelled()) {
                                switch (eventType) {
                                    case XmlPullParser.END_TAG:
                                        name = parser.getName();
                                        if (name.equals("event")) {
                                            sessions.add(session);
                                            isSessionDone = true;
                                        }
                                        break;
                                    case XmlPullParser.START_TAG:
                                        name = parser.getName();
                                        //noinspection IfCanBeSwitch
                                        if (name.equals("title")) {
                                            parser.next();
                                            session.setTitle(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("subtitle")) {
                                            parser.next();
                                            session.setSubtitle(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("slug")) {
                                            parser.next();
                                            session.setSlug(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("feedback_url")) {
                                            parser.next();
                                            session.setFeedbackUrl(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("url")) {
                                            parser.next();
                                            session.setUrl(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("track")) {
                                            parser.next();
                                            session.setTrack(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("type")) {
                                            parser.next();
                                            session.setType(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("language")) {
                                            parser.next();
                                            session.setLanguage(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("abstract")) {
                                            parser.next();
                                            session.setAbstractt(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("description")) {
                                            parser.next();
                                            session.setDescription(XmlPullParsers.getSanitizedText(parser));
                                        } else if (name.equals("person")) {
                                            parser.next();
                                            String separator = !session.getSpeakers().isEmpty() ? ";" : "";
                                            session.setSpeakers(session.getSpeakers() + separator + XmlPullParsers.getSanitizedText(parser));
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
                                            if (!session.getLinks().isEmpty()) {
                                                sb.append(session.getLinks());
                                                sb.append(",");
                                            }
                                            sb.append("[").append(urlName).append("]").append("(")
                                                    .append(url).append(")");
                                            session.setLinks(sb.toString());
                                        } else if (name.equals("start")) {
                                            parser.next();
                                            session.setStartTime(DateParser.getMinutes(XmlPullParsers.getSanitizedText(parser)));
                                            session.setRelativeStartTime(session.getStartTime());
                                            if (session.getRelativeStartTime() < dayChangeTime) {
                                                session.setRelativeStartTime(session.getRelativeStartTime() + MINUTES_OF_ONE_DAY);
                                            }
                                        } else if (name.equals("duration")) {
                                            parser.next();
                                            int minutes = DurationParser.getMinutes(XmlPullParsers.getSanitizedText(parser));
                                            session.setDuration(minutes);
                                        } else if (name.equals("date")) {
                                            parser.next();
                                            String sanitizedText = XmlPullParsers.getSanitizedText(parser);
                                            session.setDateUTC(DateParser.getDateTime(sanitizedText));
                                            session.setTimeZoneOffset(info.metadude.android.eventfahrplan.commons.temporal.DateParser.parseTimeZoneOffset(sanitizedText));
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
                                                            session.setRecordingLicense(XmlPullParsers.getSanitizedText(parser));
                                                        } else if (name.equals("optout")) {
                                                            parser.next();
                                                            session.setRecordingOptOut(Boolean.parseBoolean(XmlPullParsers.getSanitizedText(parser)));
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
                                if (isSessionDone) {
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
                                            dayChangeTime = DateParser.getMinutes(XmlPullParsers.getSanitizedText(parser));
                                        }
                                        if (name.equals("time_zone_name")) {
                                            parser.next();
                                            meta.setTimeZoneName(XmlPullParsers.getSanitizedText(parser));
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
