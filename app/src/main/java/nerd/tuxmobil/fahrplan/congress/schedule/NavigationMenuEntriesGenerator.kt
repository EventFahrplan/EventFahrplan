@file:JvmName("NavigationMenuEntriesGenerator")

package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.models.DateInfos

/**
 * Returns a [String] array of day menu entries.
 *
 * If the [currentDate] matches a date in [dateInfos] then the corresponding day will be suffixed
 * with the given [todayString]. Example: ["Day 1", "Day 2 - Today", "Day 3"]
 * An [IllegalArgumentException] is thrown the parameter restrictions are not met.
 *
 * @param numDays Number of days. Must be 1 or more.
 * @param dateInfos A list of [DateInfo] objects.
 *                  The [dayIdx] of the first object must be 1.
 *                  The list cannot be null nor empty.
 * @param currentDate A formatted date string. Pattern: YYYY-MM-DD.
 * @param todayString The word "Day" in the language of choice.
 * @param todayString The word "Today" in the language of choice.
 */
internal fun getDayMenuEntries(numDays: Int, dateInfos: DateInfos?, currentDate: String, dayString: String, todayString: String): Array<String?> {
    if (numDays < 1) {
        throw IllegalArgumentException("Invalid number of days: $numDays")
    }
    if (dateInfos == null || dateInfos.isEmpty()) {
        throw IllegalArgumentException("Invalid date info list: $dateInfos")
    }
    val entries = arrayOfNulls<String>(numDays)
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
        entries[dayIndex] = entry
    }
    return entries
}

