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
        startsAtOrBefore(time) && Moment.ofEpochMilli(time).isBefore(lastDayEndTime)

    fun endsAtOrBefore(time: Long) = with(Moment.ofEpochMilli(time)) {
        lastDayEndTime.isSimultaneousWith(this) || lastDayEndTime.isBefore(this)
    }

    fun startsAfter(time: Long) =
        firstDayStartTime.isAfter(Moment.ofEpochMilli(time))

    fun startsAtOrBefore(time: Long) = with(Moment.ofEpochMilli(time)) {
        firstDayStartTime.isSimultaneousWith(this) || firstDayStartTime.isBefore(this)
    }

    override fun toString() =
        "firstDayStartTime = $firstDayStartTime, lastDayEndTime = $lastDayEndTime"

}
