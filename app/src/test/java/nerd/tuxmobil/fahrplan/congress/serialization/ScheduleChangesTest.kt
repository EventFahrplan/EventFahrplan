package nerd.tuxmobil.fahrplan.congress.serialization

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class ScheduleChangesTest {

    private val oldLectures = ArrayList<Lecture>()
    private val newLectures = ArrayList<Lecture>()

    @Test
    fun hasScheduleChangedWithSameId() {
        oldLectures.add(Lecture("lectureId3"))
        newLectures.add(Lecture("lectureId3"))
        assertThat(ScheduleChanges.hasScheduleChanged(newLectures, oldLectures)).isFalse()
    }

    @Test
    fun hasScheduleChangedWithNewId() {
        oldLectures.add(Lecture("lectureId3"))
        newLectures.add(Lecture("lectureId7"))
        assertThat(ScheduleChanges.hasScheduleChanged(newLectures, oldLectures)).isTrue()
    }

    @Test
    fun hasScheduleChangedWithOddTitles() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.title = "Old title"
        newLecture.title = "New title"
    }

    @Test
    fun hasScheduleChangedWithOddSubtitles() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.subtitle = "Old subtitle"
        newLecture.subtitle = "New subtitle"
    }

    @Test
    fun hasScheduleChangedWithOddSpeakers() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.speakers = "Old speakers"
        newLecture.speakers = "New speakers"
    }

    @Test
    fun hasScheduleChangedWithOddLanguages() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.lang = "de"
        newLecture.lang = "en"
    }

    @Test
    fun hasScheduleChangedWithOddRooms() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.room = "Room 1"
        newLecture.room = "Room A"
    }

    @Test
    fun hasScheduleChangedWithOddTracks() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.track = "hardware"
        newLecture.track = "software"
    }

    @Test
    fun hasScheduleChangedWithOddRecordingOptOut() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.recordingOptOut = true
        newLecture.recordingOptOut = false
    }

    @Test
    fun hasScheduleChangedWithOddDays() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.day = 4
        newLecture.day = 1
    }

    @Test
    fun hasScheduleChangedWithOddStartTimes() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.startTime = 1185
        newLecture.startTime = 1410
    }

    @Test
    fun hasScheduleChangedWithOddDurations() = assertLectureHasChanged { oldLecture, newLecture ->
        oldLecture.duration = 30
        newLecture.duration = 60
    }

    private fun assertLectureHasChanged(modify: (oldLecture: Lecture, newLecture: Lecture) -> Unit) {
        val oldLecture = Lecture("lectureId3")
        val newLecture = Lecture("lectureId3")
        modify.invoke(oldLecture, newLecture)
        oldLectures.add(oldLecture)
        newLectures.add(newLecture)
        assertThat(ScheduleChanges.hasScheduleChanged(newLectures, oldLectures)).isTrue()
    }

}
