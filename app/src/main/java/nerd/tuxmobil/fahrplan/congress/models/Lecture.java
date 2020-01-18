package nerd.tuxmobil.fahrplan.congress.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.threeten.bp.Instant;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.R;

public class Lecture {

    public String title;
    public String subtitle;
    public String url;
    public int day;
    public String date;                 // YYYY-MM-DD
    public long dateUTC;                // milliseconds
    public int startTime;               // minutes since day start
    public int relStartTime;            // minutes since conference start
    public int duration;                // minutes

    public String room;
    public int roomIndex;

    public String speakers;
    public String track;
    public String lectureId;
    public String type;
    public String lang;
    public String slug;
    public String abstractt;
    public String description;

    public String links;

    public boolean highlight;
    public boolean hasAlarm;

    public String recordingLicense;
    public boolean recordingOptOut;

    public boolean changedTitle;
    public boolean changedSubtitle;
    public boolean changedRoom;
    public boolean changedDay;
    public boolean changedTime;
    public boolean changedDuration;
    public boolean changedSpeakers;
    public boolean changedRecordingOptOut;
    public boolean changedLanguage;
    public boolean changedTrack;
    public boolean changedIsNew;
    public boolean changedIsCanceled;

    private static final boolean RECORDING_OPTOUT_OFF = false;

    public Lecture(String lectureId) {
        title = "";
        subtitle = "";
        day = 0;
        room = "";
        slug = "";
        startTime = 0;
        duration = 0;
        speakers = "";
        track = "";
        type = "";
        lang = "";
        abstractt = "";
        description = "";
        relStartTime = 0;
        links = "";
        date = "";
        this.lectureId = lectureId;
        highlight = false;
        hasAlarm = false;
        dateUTC = 0;
        roomIndex = 0;
        recordingLicense = "";
        recordingOptOut = RECORDING_OPTOUT_OFF;
        changedTitle = false;
        changedSubtitle = false;
        changedRoom = false;
        changedDay = false;
        changedSpeakers = false;
        changedRecordingOptOut = false;
        changedLanguage = false;
        changedTrack = false;
        changedIsNew = false;
        changedTime = false;
        changedDuration = false;
        changedIsCanceled = false;
    }

    @NonNull
    public String getLinks() {
        return links == null ? "" : links;
    }

    public Moment getMoment() {
        Moment moment = new Moment(Instant.parse(date).toEpochMilli());
        moment.plusSeconds(relStartTime * 60);
        String[] splitDate = date.split("-");
        return moment;
    }

    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lecture lecture = (Lecture) o;

        if (day != lecture.day) return false;
        if (duration != lecture.duration) return false;
        if (recordingOptOut != lecture.recordingOptOut) return false;
        if (startTime != lecture.startTime) return false;
        if (date != null ? !date.equals(lecture.date) : lecture.date != null) return false;
        if (lang != null ? !lang.equals(lecture.lang) : lecture.lang != null) return false;
        if (!lectureId.equals(lecture.lectureId)) return false;
        if (recordingLicense != null ? !recordingLicense.equals(lecture.recordingLicense) :
                lecture.recordingLicense != null)
            return false;
        if (room != null ? !room.equals(lecture.room) : lecture.room != null) return false;
        if (speakers != null ? !speakers.equals(lecture.speakers) : lecture.speakers != null)
            return false;
        if (subtitle != null ? !subtitle.equals(lecture.subtitle) : lecture.subtitle != null)
            return false;
        if (!title.equals(lecture.title)) return false;
        if (track != null ? !track.equals(lecture.track) : lecture.track != null) return false;
        if (type != null ? !type.equals(lecture.type) : lecture.type != null) return false;
        if (dateUTC != lecture.dateUTC) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (subtitle != null ? subtitle.hashCode() : 0);
        result = 31 * result + day;
        result = 31 * result + (room != null ? room.hashCode() : 0);
        result = 31 * result + startTime;
        result = 31 * result + duration;
        result = 31 * result + (speakers != null ? speakers.hashCode() : 0);
        result = 31 * result + (track != null ? track.hashCode() : 0);
        result = 31 * result + lectureId.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (recordingLicense != null ? recordingLicense.hashCode() : 0);
        result = 31 * result + (recordingOptOut ? 1 : 0);
        result = 31 * result + (int) dateUTC;
        return result;
    }

    public void cancel() {
        changedIsCanceled = true;
        changedTitle = false;
        changedSubtitle = false;
        changedRoom = false;
        changedDay = false;
        changedSpeakers = false;
        changedRecordingOptOut = false;
        changedLanguage = false;
        changedTrack = false;
        changedIsNew = false;
        changedTime = false;
        changedDuration = false;
    }

    public String getChangedStateString() {
        return "Lecture{" +
                "changedTitle=" + changedTitle +
                ", changedSubtitle=" + changedSubtitle +
                ", changedRoom=" + changedRoom +
                ", changedDay=" + changedDay +
                ", changedTime=" + changedTime +
                ", changedDuration=" + changedDuration +
                ", changedSpeakers=" + changedSpeakers +
                ", changedRecordingOptOut=" + changedRecordingOptOut +
                ", changedLanguage=" + changedLanguage +
                ", changedTrack=" + changedTrack +
                ", changedIsNew=" + changedIsNew +
                ", changedIsCanceled=" + changedIsCanceled +
                '}';
    }

    public boolean isChanged() {
        return changedDay || changedDuration ||
                changedLanguage || changedRecordingOptOut ||
                changedRoom || changedSpeakers || changedSubtitle ||
                changedTime || changedTitle || changedTrack;
    }

    public String getFormattedSpeakers() {
        return speakers.replaceAll(";", ", ");
    }

    public String getFormattedTrackText() {
        StringBuilder builder = new StringBuilder();
        builder.append(track);
        if (!TextUtils.isEmpty(lang)) {
            builder.append(" [").append(lang).append("]");
        }
        return builder.toString();
    }

    @NonNull
    public String getFormattedTrackContentDescription(@NonNull Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(track);
        if (!TextUtils.isEmpty(lang)) {
            builder.append("; ").append(getLanguageContentDescription(context));
        }
        return builder.toString();
    }

    @NonNull
    public String getLanguageContentDescription(@NonNull Context context) {
        if (TextUtils.isEmpty(lang)) {
            return context.getString(R.string.lecture_list_item_language_unknown_content_description);
        }
        if ("en".equals(lang)) {
            return context.getString(R.string.lecture_list_item_language_english_content_description);
        }
        if ("de".equals(lang)) {
            return context.getString(R.string.lecture_list_item_language_german_content_description);
        }
        if ("pt".equals(lang)) {
            return context.getString(R.string.lecture_list_item_language_portuguese_content_description);
        }
        return context.getString(R.string.lecture_list_item_language_undefined_content_description, lang);
    }

    public void shiftRoomIndexBy(int amount) {
        roomIndex += amount;
    }

}
