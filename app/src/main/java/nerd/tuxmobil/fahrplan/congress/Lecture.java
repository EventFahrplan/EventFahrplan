package nerd.tuxmobil.fahrplan.congress;

import android.text.format.Time;

public class Lecture {

    public String title;

    public String subtitle;

    public int day;

    public String room;

    public int startTime;                // minutes since day start

    public int duration;                // minutes

    public String speakers;

    public String track;

    public String lecture_id;

    public String type;

    public String lang;

    public String abstractt;

    public String description;

    public int relStartTime;

    public String links;

    public String date;

    public boolean highlight;

    public boolean has_alarm;

    public long dateUTC;

    public int room_index;

    public String recordingLicense;

    public boolean recordingOptOut;

    public static final boolean RECORDING_OPTOUT_ON = true;

    public static final boolean RECORDING_OPTOUT_OFF = false;

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

    public Lecture(String lecture_id) {
        title = "";
        subtitle = "";
        day = 0;
        room = "";
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
        this.lecture_id = lecture_id;
        highlight = false;
        has_alarm = false;
        dateUTC = 0;
        room_index = 0;
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

    public static int parseStartTime(String text) {
        String time[] = text.split(":");
        return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
    }

    public static int parseDuration(String text) {
        String time[] = text.split(":");
        return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
    }

    public Time getTime() {
        Time t = new Time();
        String[] splitDate = date.split("-");
        t.setToNow();
        t.year = Integer.parseInt(splitDate[0]);
        t.month = Integer.parseInt(splitDate[1]) - 1;
        t.monthDay = Integer.parseInt(splitDate[2]);
        t.hour = relStartTime / 60;
        t.minute = relStartTime % 60;

        return t;
    }

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
        if (!lecture_id.equals(lecture.lecture_id)) return false;
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
        result = 31 * result + lecture_id.hashCode();
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

    public boolean isChanged() {
        if ((changedDay || changedDuration ||
                changedLanguage || changedRecordingOptOut ||
                changedRoom || changedSpeakers || changedSubtitle ||
                changedTime || changedTitle || changedTrack)) {
            return true;
        }
        return false;
    }

    public String getFormattedSpeakers() {
        return speakers.replaceAll(";", ", ");
    }

}
