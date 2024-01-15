package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.dataconverters.toVirtualDays
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.models.VirtualDay

internal class NavigationMenuEntriesGenerator @JvmOverloads constructor(

    /**
     * The word "Day" in the language of choice.
     */
    private val dayString: String,
    /**
     * The word "Today" in the language of choice.
     */
    private val todayString: String,

    private val logging: Logging = Logging.get()

) {

    private companion object {
        const val LOG_TAG = "NavigationMenuEntriesGenerator"
    }

    /**
     * Returns a [String] array of day menu entries.
     *
     * If the [currentDate] is within the time frame of a generated day then the corresponding
     * day will be suffixed with the given [todayString]. Example: ["Day 1", "Day 2 - Today", "Day 3"]
     * An [IllegalArgumentException] is thrown the parameter restrictions are not met.
     *
     * @param numDays Expected number of days as outlined in the schedule. Sessions or days
     * might still be missing. Must be 0 or more.
     * @param sessions A list of session of all days. The list cannot be null.
     * @param currentDate A moment instance representing the day of interest.
     */
    fun getDayMenuEntries(
        numDays: Int,
        sessions: List<Session>,
        currentDate: Moment = Moment.now()
    ): List<String> {
        if (numDays < 0) {
            throw IllegalArgumentException("Number of days is $numDays but must be 0 or more.")
        }
        val virtualDays = sessions.toVirtualDays()
        virtualDays.forEach { logging.d(LOG_TAG, "$it") }
        if (numDays < virtualDays.size) {
            throw IllegalArgumentException("Expected maximum $numDays day(s) but days list contains ${virtualDays.size} items.")
        }
        logging.d(LOG_TAG, "Today is $currentDate")
        val menuEntries = virtualDays.toMenuEntries(
            dayString = dayString,
            todayString = todayString,
            currentDate = currentDate
        )
        return menuEntries
    }

}

private fun List<VirtualDay>.toMenuEntries(
    dayString: String,
    todayString: String,
    currentDate: Moment
): List<String> {
    return map { day ->
        var entry = "$dayString ${day.index}"
        if (currentDate in day.timeFrame) {
            entry += " - $todayString"
        }
        entry
    }
}
