package nerd.tuxmobil.fahrplan.congress.serialization;

import android.support.annotation.NonNull;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.models.Lecture;

import static kotlin.collections.CollectionsKt.singleOrNull;

public class ScheduleChanges {

    public static boolean hasScheduleChanged(@NonNull List<Lecture> lectures,
                                             @NonNull List<Lecture> oldLectures) {
        if (oldLectures.isEmpty()) {
            return false;
        }
        boolean changed = false;

        int lectureIndex = oldLectures.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = oldLectures.get(lectureIndex);
            if (l.changedIsCanceled) oldLectures.remove(lectureIndex);
            lectureIndex--;
        }

        for (lectureIndex = 0; lectureIndex < lectures.size(); lectureIndex++) {
            Lecture newLecture = lectures.get(lectureIndex);
            Lecture oldLecture = singleOrNull(oldLectures, lecture -> newLecture.lectureId.equals(lecture.lectureId));

            if (oldLecture == null) {
                newLecture.changedIsNew = true;
                changed = true;
                continue;
            }

            if (oldLecture.equals(newLecture)) {
                oldLectures.remove(oldLecture);
                continue;
            }

            if (!newLecture.title.equals(oldLecture.title)) {
                newLecture.changedTitle = true;
                changed = true;
            }
            if (!newLecture.subtitle.equals(oldLecture.subtitle)) {
                newLecture.changedSubtitle = true;
                changed = true;
            }
            if (!newLecture.speakers.equals(oldLecture.speakers)) {
                newLecture.changedSpeakers = true;
                changed = true;
            }
            if (!newLecture.lang.equals(oldLecture.lang)) {
                newLecture.changedLanguage = true;
                changed = true;
            }
            if (!newLecture.room.equals(oldLecture.room)) {
                newLecture.changedRoom = true;
                changed = true;
            }
            if (!newLecture.track.equals(oldLecture.track)) {
                newLecture.changedTrack = true;
                changed = true;
            }
            if (newLecture.recordingOptOut != oldLecture.recordingOptOut) {
                newLecture.changedRecordingOptOut = true;
                changed = true;
            }
            if (newLecture.day != oldLecture.day) {
                newLecture.changedDay = true;
                changed = true;
            }
            if (newLecture.startTime != oldLecture.startTime) {
                newLecture.changedTime = true;
                changed = true;
            }
            if (newLecture.duration != oldLecture.duration) {
                newLecture.changedDuration = true;
                changed = true;
            }
            oldLectures.remove(oldLecture);
        }

        for (lectureIndex = 0; lectureIndex < oldLectures.size(); lectureIndex++) {
            Lecture oldLecture = oldLectures.get(lectureIndex);
            oldLecture.cancel();
            lectures.add(oldLecture);
            changed = true;
        }

        return changed;
    }

}
