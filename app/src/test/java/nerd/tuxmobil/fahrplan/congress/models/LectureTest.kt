package nerd.tuxmobil.fahrplan.congress.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LectureTest {

    @Test
    fun getStartTimeMoment() {
        val lecture = Lecture("1")
        lecture.relStartTime = 121
        lecture.date = "2019-12-27"

        val moment = lecture.startTimeMoment
        assertThat(moment.minute).isEqualTo(1)
        assertThat(moment.minuteOfDay).isEqualTo(121)
        assertThat(moment.hour).isEqualTo(2)
        assertThat(moment.month).isEqualTo(12)
        assertThat(moment.monthDay).isEqualTo(27)
        assertThat(moment.year).isEqualTo(2019)
    }
}
