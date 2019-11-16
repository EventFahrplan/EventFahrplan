package nerd.tuxmobil.fahrplan.congress.models

import org.threeten.bp.ZonedDateTime

data class DayRange(

        val startsAt: ZonedDateTime,
        val endsAt: ZonedDateTime

) {

    fun contains(dateTime: ZonedDateTime) =
            this.startsAt <= dateTime && dateTime <= this.endsAt

}
