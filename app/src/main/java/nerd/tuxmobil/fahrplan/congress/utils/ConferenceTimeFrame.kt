package nerd.tuxmobil.fahrplan.congress.utils

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmUpdater
import nerd.tuxmobil.fahrplan.congress.schedule.Conference

/**
 * Represents the time frame of a conference which is taken into account by the [AlarmUpdater]
 * to calculate if and when alarms should fire.
 *
 * This class should not be used for other purposes.
 * It is not a replacement for the [Conference] class.
 */
// TODO Merge with Conference class?
class ConferenceTimeFrame(

    val firstDayStartTime: Moment,
    private val lastDayEndTime: Moment

) {

    init {
        check(isValid) { "Invalid conference time frame: $this" }
    }

    val isValid: Boolean
        get() = firstDayStartTime.isBefore(lastDayEndTime)

    operator fun contains(moment: Moment) =
        startsAtOrBefore(moment) && lastDayEndTime.isAfter(moment)

    fun endsAtOrBefore(moment: Moment) =
        lastDayEndTime.isSimultaneousWith(moment) || lastDayEndTime.isBefore(moment)

    fun startsAfter(moment: Moment) =
        firstDayStartTime.isAfter(moment)

    fun startsAtOrBefore(moment: Moment) =
        firstDayStartTime.isSimultaneousWith(moment) || firstDayStartTime.isBefore(moment)

    override fun toString() =
        "firstDayStartTime = $firstDayStartTime, lastDayEndTime = $lastDayEndTime"

}
