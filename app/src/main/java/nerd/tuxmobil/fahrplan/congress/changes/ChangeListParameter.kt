package nerd.tuxmobil.fahrplan.congress.changes

import nerd.tuxmobil.fahrplan.congress.models.Session

/**
 * Payload of the observable property in the [ChangeListViewModel]
 * which is observed by the [ChangeListFragment].
 */
data class ChangeListParameter(

    val sessions: List<Session>,
    val numDay: Int,
    val useDeviceTimeZone: Boolean

)
