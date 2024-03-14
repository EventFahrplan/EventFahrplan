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

    firstDayStartTime: Moment,
    lastDayEndTime: Moment

) {

    private val timeFrame = firstDayStartTime..lastDayEndTime

    init {
        check(isValid) { "Invalid conference time frame: $this" }
    }

    val firstDayStartTime: Moment
        get() = timeFrame.start

    val isValid: Boolean
        get() = timeFrame.start.isBefore(timeFrame.endInclusive)

    operator fun contains(moment: Moment) =
        startsAtOrBefore(moment) && timeFrame.endInclusive.isAfter(moment)

    fun endsAtOrBefore(moment: Moment) =
        timeFrame.endInclusive.isSimultaneousWith(moment) || timeFrame.endInclusive.isBefore(moment)

    fun startsAfter(moment: Moment) =
        timeFrame.start.isAfter(moment)

    fun startsAtOrBefore(moment: Moment) =
        timeFrame.start.isSimultaneousWith(moment) || timeFrame.start.isBefore(moment)

    override fun toString() =
        timeFrame.toString()

}
