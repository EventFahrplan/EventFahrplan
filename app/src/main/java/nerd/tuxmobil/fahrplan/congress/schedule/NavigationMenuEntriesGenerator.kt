package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.dataconverters.toVirtualDays
import nerd.tuxmobil.fahrplan.congress.models.NavigationMenuDay
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.models.VirtualDay

internal class NavigationMenuEntriesGenerator(

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
     * An [IllegalArgumentException] is thrown if the parameter restrictions are not met.
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
        require(numDays >= 0) {
            "Number of days is $numDays but must be 0 or more."
        }
        val virtualDays = sessions.toVirtualDays()
        virtualDays.forEach { logging.d(LOG_TAG, "$it") }
        require(numDays >= virtualDays.size) {
            "Expected maximum $numDays day(s) but days list contains ${virtualDays.size} items."
        }
        logging.d(LOG_TAG, "Today is $currentDate")
        val menuEntries = virtualDays.toNavigationMenuDays().toMenuEntries(
            dayString = dayString,
            todayString = todayString,
            currentDate = currentDate
        )
        return menuEntries
    }

}

private fun List<VirtualDay>.toNavigationMenuDays() = map(::NavigationMenuDay)

private fun List<NavigationMenuDay>.toMenuEntries(
    dayString: String,
    todayString: String,
    currentDate: Moment
): List<String> {
    // Append the "today" suffix to the last day entry that contains the current date.
    // If no day contains the current date then no suffix is appended.
    val indexOfLast = indexOfLast { currentDate in it.timeFrame }
    return mapIndexed { index, day ->
        var entry = "$dayString ${day.index}"
        if (index == indexOfLast) {
            entry += " - $todayString"
        }
        entry
    }
}
