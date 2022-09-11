package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment

import java.util.ArrayList

class DateInfos : ArrayList<DateInfo>() {

    fun sameDay(timestamp: Moment, sessionListDay: Int): Boolean {
        val currentDate = timestamp.startOfDay()
        return any { (dayIndex, date) -> dayIndex == sessionListDay && date == currentDate }
    }

    /**
     * Returns the index of today if found, [DateInfo.DAY_INDEX_NOT_FOUND] otherwise.
     */
    val indexOfToday: Int
        get() {
            if (isEmpty()) {
                return DateInfo.DAY_INDEX_NOT_FOUND
            }

            val today = Moment.now()
                .minusHours(DAY_CHANGE_HOUR_DEFAULT.toLong())
                .minusMinutes(DAY_CHANGE_MINUTE_DEFAULT.toLong())
                .startOfDay()

            forEach { dateInfo ->
                val dayIndex = dateInfo.getDayIndex(today)
                if (dayIndex != DateInfo.DAY_INDEX_NOT_FOUND) {
                    return dayIndex
                }
            }

            return DateInfo.DAY_INDEX_NOT_FOUND
        }

    private companion object {
        const val serialVersionUID = 1L

        /**
         * Hour of day change (all sessions which start before count to the previous day).
         */
        const val DAY_CHANGE_HOUR_DEFAULT = 4

        /**
         * Minute of day change.
         */
        const val DAY_CHANGE_MINUTE_DEFAULT = 0
    }
}