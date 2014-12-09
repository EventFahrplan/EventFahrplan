package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;

public class LectureList extends ArrayList<Lecture> {

    public Lecture getLecture(String lectureId) {
        int index;
        for (index = 0; index < this.size(); index++) {
            Lecture l = this.get(index);
            if (l.lecture_id.equals(lectureId)) return l;
        }
        return null;
    }
}
