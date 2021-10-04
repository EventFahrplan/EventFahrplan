package nerd.tuxmobil.fahrplan.congress.favorites

import nerd.tuxmobil.fahrplan.congress.models.Session

data class StarredListParameter(

    val sessions: List<Session>,
    val numDay: Int,
    val useDeviceTimeZone: Boolean

)
