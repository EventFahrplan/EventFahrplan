package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.BOX_HEIGHT_MULTIPLIER
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.Companion.FIFTEEN_MINUTES
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
        var sessionStartsAt = conference.firstSessionStartsAt
        var sessionStartsAtMinutes = sessionStartsAt.minuteOfDay
        var scrollAmount = 0

        val hasNotStarted = nowMoment.minuteOfDay < sessionStartsAtMinutes
        if (!(hasNotStarted && dateInfos.sameDay(nowMoment, currentDayIndex))) {
            var timeSegment: TimeSegment
            val minutesToAdd = if (conference.spansMultipleDays) MINUTES_OF_ONE_DAY else 0
            val lastSessionEndsAtMinutes = conference.lastSessionEndsAt.minuteOfDay + minutesToAdd
            while (sessionStartsAtMinutes < lastSessionEndsAtMinutes) {
                timeSegment = TimeSegment.ofMoment(sessionStartsAt)
                scrollAmount += if (timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                    break
                } else {
                    boxHeight * BOX_HEIGHT_MULTIPLIER
                }
                sessionStartsAt = sessionStartsAt.plusMinutes(FIFTEEN_MINUTES.toLong())
                sessionStartsAtMinutes += FIFTEEN_MINUTES
            }
            var time = sessionStartsAt.minuteOfDay
            val roomDataList = scheduleData.roomDataList
            if (columnIndex >= 0 && columnIndex < roomDataList.size) {
                val roomData = roomDataList[columnIndex]
                for (session in roomData.sessions) {
                    if (session.startsAt.minuteOfDay <= time && session.endsAt.minuteOfDay > time) {
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

    /**
     * Returns the scroll amount for the given [session].
     *
     * First the duration between the given session and the first session of the day is calculated
     * at minutes precision. Then based on this duration the device specific scroll amount is
     * calculated considering the [TIME_GRID_MINIMUM_SEGMENT_HEIGHT] and the given [boxHeight].
     *
     * This calculation is independent of the time zone offset of the device nor conference.
     */
    fun calculateScrollAmount(conference: Conference, session: Session, boxHeight: Int): Int {
        val sessionStartsAt = session.startsAt
        val firstSessionStartsAt = conference.firstSessionStartsAt
        val minutes = firstSessionStartsAt.minutesUntil(sessionStartsAt).toInt()
        return minutes / TIME_GRID_MINIMUM_SEGMENT_HEIGHT * boxHeight
    }

}
