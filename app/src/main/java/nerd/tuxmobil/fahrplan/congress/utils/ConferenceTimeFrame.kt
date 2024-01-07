package nerd.tuxmobil.fahrplan.congress.utils

import info.metadude.android.eventfahrplan.commons.temporal.Moment

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

    operator fun contains(time: Long) =
        startsAtOrBefore(time) && time < lastDayEndTime.toMilliseconds()

    fun endsAtOrBefore(time: Long) = time >= lastDayEndTime.toMilliseconds()

    fun startsAfter(time: Long) = time < firstDayStartTime.toMilliseconds()

    fun startsAtOrBefore(time: Long) = time >= firstDayStartTime.toMilliseconds()

    override fun toString() =
        "firstDayStartTime = $firstDayStartTime, lastDayEndTime = $lastDayEndTime"

}
