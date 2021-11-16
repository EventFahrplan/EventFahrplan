package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.DateInfos

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
     * If the [currentDate] matches a date in [dateInfos] then the corresponding day will be suffixed
     * with the given [todayString]. Example: ["Day 1", "Day 2 - Today", "Day 3"]
     * An [IllegalArgumentException] is thrown the parameter restrictions are not met.
     *
     * @param numDays Number of days. Must be 1 or more.
     * @param dateInfos A [list of DateInfo objects][DateInfos]. The [dayIdx] of the first object
     * must be 1. The list cannot be null nor empty.
     * @param currentDate A moment instance representing the day of interest.
     */
    @JvmOverloads
    fun getDayMenuEntries(
        numDays: Int,
        dateInfos: DateInfos?,
        currentDate: Moment = Moment.now().startOfDay()
    ): List<String> {
        if (numDays < 1) {
            throw IllegalArgumentException("Invalid number of days: $numDays")
        }
        if (dateInfos == null || dateInfos.isEmpty()) {
            throw IllegalArgumentException("Invalid date info list: $dateInfos")
        }
        logging.d(LOG_TAG, "Today is " + currentDate.toUtcDateTime().toLocalDate())
        val entries = mutableListOf<String>()
        for (dayIndex in 0 until numDays) {
            var entry = "$dayString ${dayIndex + 1}"
            for (dateInfo in dateInfos) {
                if (dateInfo.dayIdx == dayIndex + 1) {
                    if (currentDate == dateInfo.date) {
                        entry += " - $todayString"
                    }
                    break
                }
            }
            entries.add(dayIndex, entry)
        }
        return entries.toList()
    }

}
