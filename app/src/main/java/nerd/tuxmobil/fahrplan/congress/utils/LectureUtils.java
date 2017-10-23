package nerd.tuxmobil.fahrplan.congress.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.models.Lecture;

public class LectureUtils {

    @Nullable
    public static Lecture getLecture(@NonNull List<Lecture> lectures, @NonNull String lectureId) {
        for (int index = 0; index < lectures.size(); index++) {
            Lecture lecture = lectures.get(index);
            if (lectureId.equals(lecture.lecture_id)) {
                return lecture;
            }
        }
        return null;
    }

}
