package nerd.tuxmobil.fahrplan.congress.schedule

import android.widget.LinearLayout
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.schedule.LectureViewDrawer.Companion.DIVISOR
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LectureViewDrawerTest {
    private val standardHeight = 1
    private val conference = Conference()
    private val conferenceDate = "2020-03-30"
    private var lectureId = 0

    private fun createLecture(roomIndex: Int, date: String? = null, startTime: Int = 0, duration: Int = 0): Lecture {
        val lecture = Lecture((lectureId++).toString())

        if (date != null) {
            val dateUTC = Moment(date)
            dateUTC.plusMinutes(startTime.toLong())
            lecture.dateUTC = dateUTC.toMilliseconds()
        } else {
            lecture.relStartTime = startTime
        }

        return lecture.apply { this.roomIndex = roomIndex; this.duration = duration }
    }

    @Test
    fun `calculateLayoutParams for empty list returns empty params`() {
        val lectures = listOf<Lecture>()

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)

        assertThat(layoutParams).isEmpty()
    }

    @Test
    fun `calculateLayoutParams returns only lectures in given room`() {
        val lectures = listOf(createLecture(roomIndex = 0), createLecture(roomIndex = 1))
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)

        assertThat(layoutParams.size).isEqualTo(1)
        assertThat(layoutParams[lectures.first()]).isNotNull()
    }

    @Test
    fun `calculateLayoutParams for single lecture returns margins 0`() {
        val lectures = listOf(createLecture(roomIndex = 0))
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lectureParams = layoutParams[lectures.first()]

        assertMargins(lectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for single UTC lecture sets top margin 0 (its the first lecture in all rooms, so on the top)`() {
        val startTime = 10 * 60 // 10:00am
        val lectures = listOf(createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime))
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lectureParams = layoutParams[lectures.first()]

        assertMargins(lectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for single *none* UTC lecture sets top margin 0 (its the first lecture in all rooms, so on the top)`() {
        val startTime = 10 * 60 // 10:00am
        val lectures = listOf(createLecture(roomIndex = 0, startTime = startTime))
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lectureParams = layoutParams[lectures.first()]

        assertMargins(lectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for consecutive lecture sets margins based on gap duration`() {
        val startTime1 = 10 * 60 // 10:00am
        val duration1 = 45
        val gapMinutes = 15
        val startTime2 = startTime1 + duration1 + gapMinutes // 11:00am

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lecture1Params = layoutParams[lecture1]
        val secondLectureParams = layoutParams[lecture2]

        assertMargins(lecture1Params, 0, gapMinutes)
        assertMargins(secondLectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams for consecutive lecture in another room sets top margin based on conference day start`() {
        /*
                         room 0             room 1
                   +---------------------------------------+
            10:00  +-------------------+                   |  +
                   |                   |                   |  |
                   |     lecture 0     |                   |  |
                   |                   |                   |  | marginTop
            10:45  +-------------------+                   |  |
                   |                   |                   |  |
            11:00  |                   +-------------------+  +
                   |                   |                   |
                   |                   |    lecture 1      |
                   |                   |                   |

        * lecture 1 follows directly lecture 0, but in another room, hence the margin includes height of lecture 0.
        */
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 + 15 // 11:00am

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 1, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(1, lectures, standardHeight, conference, NoLogging)
        val secondLectureParams = layoutParams[lecture2]
        val gapMinutes = 60

        assertMargins(secondLectureParams, gapMinutes, 0)
    }

    @Test
    fun `calculateLayoutParams consecutive lecture after midnight in another room`() {
        val duration1 = 45
        val startTime1 = 23 * 60 // 11:00pm
        val startTime2 = startTime1 + duration1 + 20 // 00:05am, next day

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 1, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParamsRoom1 = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val layoutParamsRoom2 = LectureViewDrawer.calculateLayoutParams(1, lectures, standardHeight, conference, NoLogging)
        val lecture1Params = layoutParamsRoom1[lecture1]
        val secondLectureParams = layoutParamsRoom2[lecture2]
        val gapMinutes = 5 + 60 // 5 minutes in new day. 60 minutes on previous day, from lecture1, which starts at 11am

        assertMargins(lecture1Params, 0, 0)
        assertMargins(secondLectureParams, gapMinutes, 0)
    }

    @Test
    fun `calculateLayoutParams consecutive lecture after midnight in same room`() {
        val duration1 = 45
        val startTime1 = 23 * 60 // 11:00pm
        val startTime2 = startTime1 + duration1 + 30 // 00:15am, next day

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lecture1Params = layoutParams[lecture1]
        val secondLectureParams = layoutParams[lecture2]
        val gapMinutes = 30

        assertMargins(lecture1Params, 0, gapMinutes)
        assertMargins(secondLectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams overlapping lecture in same room - should cut first lecture duration to match next lecture start`() {
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 - 10 // 10:35am (10 minutes overlap)

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParams = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val lecture1Params = layoutParams[lecture1]
        val secondLectureParams = layoutParams[lecture2]

        assertMargins(lecture1Params, 0, 0)
        assertMargins(secondLectureParams, 0, 0)
    }

    @Test
    fun `calculateLayoutParams overlapping lecture in another room - should not cut any lecture`() {
        val duration1 = 45
        val startTime1 = 10 * 60 // 10:00am
        val startTime2 = startTime1 + duration1 - 10 // 10:35am (10 minutes overlap)

        val lecture1 = createLecture(roomIndex = 0, date = conferenceDate, startTime = startTime1, duration = duration1)
        val lecture2 = createLecture(roomIndex = 1, date = conferenceDate, startTime = startTime2)
        val lectures = listOf(lecture1, lecture2)
        conference.calculateTimeFrame(lectures) { Moment(it).minuteOfDay }

        val layoutParamsRoom1 = LectureViewDrawer.calculateLayoutParams(0, lectures, standardHeight, conference, NoLogging)
        val layoutParamsRoom2 = LectureViewDrawer.calculateLayoutParams(1, lectures, standardHeight, conference, NoLogging)
        val lecture1Params = layoutParamsRoom1[lecture1]
        val secondLectureParams = layoutParamsRoom2[lecture2]

        assertMargins(lecture1Params, 0, 0)
        assertMargins(secondLectureParams, 35, 0)
    }

    private fun assertMargins(lectureParams: LinearLayout.LayoutParams?, top: Int, bottom: Int) {
        assertThat(lectureParams!!).isNotNull()
        assertThat(lectureParams.topMargin).isEqualTo(standardHeight * top / DIVISOR)
        assertThat(lectureParams.bottomMargin).isEqualTo(standardHeight * bottom / DIVISOR)
    }

    object NoLogging : Logging {
        override fun d(tag: String, message: String) = Unit
        override fun e(tag: String, message: String) = Unit
        override fun report(tag: String, message: String) = Unit
    }
}