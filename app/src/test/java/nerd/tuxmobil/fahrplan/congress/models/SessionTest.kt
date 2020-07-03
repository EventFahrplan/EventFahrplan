package nerd.tuxmobil.fahrplan.congress.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {

    @Test
    fun getStartTimeMoment() {
        val lecture = Session("1")
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

    @Test
    fun `startTimeMilliseconds returns the "dateUTC" value when "dateUTC" is set`() {
        val lecture = Session("1").apply {
            dateUTC = 1
            date = "2020-03-20"
        }
        assertThat(lecture.startTimeMilliseconds).isEqualTo(1)
    }

    @Test
    fun `startTimeMilliseconds returns the "date" value when "dateUTC" is not set`() {
        val lecture = Session("1").apply {
            dateUTC = 0
            date = "2020-03-20"
        }
        assertThat(lecture.startTimeMilliseconds).isEqualTo(1584662400000L)
    }

}
