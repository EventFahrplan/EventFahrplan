package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.BOX_HEIGHT_MULTIPLIER
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.FIFTEEN_MINUTES
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.ONE_DAY
import nerd.tuxmobil.fahrplan.congress.schedule.TimeSegment.Companion.TIME_GRID_MINIMUM_SEGMENT_HEIGHT

/**
 * Calculates the amount to be scrolled depending on the given schedule data,
 * device specifics and the current date/time.
 */
internal class ScrollAmountCalculator(

        private val logging: Logging

) {

    private companion object {
        const val LOG_TAG = "ScrollAmountCalculator"
    }

    /**
     * Returns the amount to be scrolled. Valid values are 0 and positive integers.
     */
    fun calculateScrollAmount(
        conference: Conference,
        dateInfos: DateInfos,
        scheduleData: ScheduleData,
        nowMoment: Moment,
        currentDayIndex: Int,
        boxHeight: Int,
        columnIndex: Int
    ): Int {
        var time = conference.firstSessionStartsAt
        var printTime = time
        var scrollAmount = 0

        val hasStarted = nowMoment.minuteOfDay < conference.firstSessionStartsAt
        if (!(hasStarted && dateInfos.sameDay(nowMoment, currentDayIndex))) {
            var timeSegment: TimeSegment
            while (time < conference.lastSessionEndsAt) {
                timeSegment = TimeSegment.ofMinutesOfTheDay(printTime)
                val isMatched = timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)
                scrollAmount += if (isMatched) {
                    break
                } else {
                    boxHeight * BOX_HEIGHT_MULTIPLIER
                }
                time += FIFTEEN_MINUTES
                printTime = time
                if (printTime >= ONE_DAY) {
                    printTime -= ONE_DAY
                }
            }
            val roomDataList = scheduleData.roomDataList
            if (columnIndex >= 0 && columnIndex < roomDataList.size) {
                val roomData = roomDataList[columnIndex]
                for (session in roomData.sessions) {
                    if (session.startTime <= time && session.endsAtTime > time) {
                        logging.d(LOG_TAG, session.title)
                        logging.d(LOG_TAG, "$time ${session.startTime}/${session.duration}")
                        scrollAmount -= (time - session.startTime) / TIME_GRID_MINIMUM_SEGMENT_HEIGHT * boxHeight
                        time = session.startTime
                    }
                }
            }
        }
        return scrollAmount
    }
}
