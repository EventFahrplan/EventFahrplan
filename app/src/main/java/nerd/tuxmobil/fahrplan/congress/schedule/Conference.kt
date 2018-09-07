package nerd.tuxmobil.fahrplan.congress.schedule

data class Conference(

        var firstEventStartsAt: Int = 0,
        var lastEventEndsAt: Int = 0

) {

    fun lastEventEndsBeforeFirstEventStarts() = lastEventEndsAt < firstEventStartsAt

    fun forwardLastEventEndsAtByOneDay() {
        lastEventEndsAt += ONE_DAY
    }

    companion object {
        private const val ONE_DAY = 24 * 60
    }

}
