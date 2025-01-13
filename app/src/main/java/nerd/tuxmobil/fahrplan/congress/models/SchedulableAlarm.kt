package nerd.tuxmobil.fahrplan.congress.models

data class SchedulableAlarm(

        val day: Int,
        val guid: String,
        val sessionTitle: String,
        val startTime: Long

)
