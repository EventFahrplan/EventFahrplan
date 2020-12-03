package nerd.tuxmobil.fahrplan.congress.utils

// TODO Use Moment class, merge with Conference class?
class ConferenceTimeFrame(

        val firstDayStartTime: Long,
        private val lastDayEndTime: Long

) {

    init {
        check(isValid) { "Invalid conference time frame: $this" }
    }

    val isValid: Boolean
        get() = firstDayStartTime.compareTo(lastDayEndTime) == -1

    operator fun contains(time: Long) = startsAtOrBefore(time) && time < lastDayEndTime

    fun endsBefore(time: Long) = time >= lastDayEndTime

    fun startsAfter(time: Long) = time < firstDayStartTime

    fun startsAtOrBefore(time: Long) = time >= firstDayStartTime

    override fun toString() = "firstDayStartTime = $firstDayStartTime, lastDayEndTime = $lastDayEndTime"

}
