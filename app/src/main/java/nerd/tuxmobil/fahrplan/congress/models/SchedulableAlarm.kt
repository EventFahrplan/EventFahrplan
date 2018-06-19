package nerd.tuxmobil.fahrplan.congress.models

data class SchedulableAlarm(

        val day: Int,
        val eventId: String,
        val eventTitle: String,
        val startTime: Long

)
