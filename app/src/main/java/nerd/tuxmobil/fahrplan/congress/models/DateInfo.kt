package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Moment

/**
 * Represents a connection between given [date] and [dayIndex].
 * Useful to later ask for a [dayIndex] providing a date (see [getDayIndex]).
 */
data class DateInfo(

        val dayIndex: Int,
        val date: Moment

) {

    companion object {
        const val DAY_INDEX_NOT_FOUND = -1
    }

    /**
     * Retrieve day index of stored date.
     * @return if given [date] matches the stored [date]
     * returns stored [dayIndex], [DAY_INDEX_NOT_FOUND] otherwise.
     */
    fun getDayIndex(date: Moment) = if (this.date == date) dayIndex else DAY_INDEX_NOT_FOUND

}
