package nerd.tuxmobil.fahrplan.congress.schedule

import android.view.ViewGroup
import android.widget.LinearLayout
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.threeten.bp.Duration
import kotlin.collections.set

data class LayoutCalculator(val logging: Logging = Logging.get(),
                            val standardHeight: Int) {

    private companion object {
        const val LOG_TAG = "LayoutCalculator"
        const val DIVISOR = 5
        const val MILLIS_PER_MINUTE = 60000
    }

    fun calculateDisplayDistance(minutes: Int): Int {
        return standardHeight * minutes / DIVISOR
    }

    fun calculateLayoutParams(roomData: RoomData, conference: Conference): Map<Session, LinearLayout.LayoutParams> {
        val lectures = roomData.lectures
        var endTimePreviousLecture: Int = conference.firstEventStartsAt
        var startTime: Int
        var margin: Int
        var previousLecture: Session? = null
        val layoutParamsByLecture = mutableMapOf<Session, LinearLayout.LayoutParams>()

        for (lectureIndex in lectures.indices) {
            val lecture = lectures[lectureIndex]

            startTime = getStartTime(lecture, endTimePreviousLecture)

            if (startTime > endTimePreviousLecture) {
                // consecutive lecture
                margin = calculateDisplayDistance(startTime - endTimePreviousLecture)
                if (previousLecture != null) {
                    layoutParamsByLecture[previousLecture]!!.bottomMargin = margin
                    margin = 0
                }
            } else {
                // first lecture
                margin = 0
            }

            fixOverlappingEvents(lectureIndex, lectures)

            if (!layoutParamsByLecture.containsKey(lecture)) {
                layoutParamsByLecture[lecture] = createLayoutParams(lecture)
            }

            layoutParamsByLecture[lecture]!!.topMargin = margin
            endTimePreviousLecture = startTime + lecture.duration
            previousLecture = lecture
        }

        return layoutParamsByLecture
    }

    private fun createLayoutParams(lecture: Session): LinearLayout.LayoutParams {
        val height = calculateDisplayDistance(lecture.duration)
        val marginLayoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return LinearLayout.LayoutParams(marginLayoutParams)
    }

    private fun getStartTime(lecture: Session, endTimePreviousLecture: Int): Int {
        var startTime: Int
        if (lecture.dateUTC > 0) {
            startTime = Moment(lecture.dateUTC).minuteOfDay
            if (startTime < endTimePreviousLecture) {
                startTime += Duration.ofDays(1).toMinutes().toInt()
            }
        } else {
            startTime = lecture.relStartTime
        }
        return startTime
    }

    private fun fixOverlappingEvents(lectureIndex: Int, lectures: List<Session>) {
        val lecture = lectures[lectureIndex]
        val next = lectures.getOrNull(lectureIndex + 1)

        if (next != null && next.dateUTC > 0) {
            val endTimestamp = lecture.dateUTC + lecture.duration * MILLIS_PER_MINUTE
            val nextStartsBeforeCurrentEnds = endTimestamp > next.dateUTC
            if (nextStartsBeforeCurrentEnds) {
                logging.d(LOG_TAG, "${lecture.title} collides with ${next.title}")
                // cut current at the end, to match next lectures start time
                lecture.duration = ((next.dateUTC - lecture.dateUTC) / MILLIS_PER_MINUTE).toInt()
            }
        }
    }
}
