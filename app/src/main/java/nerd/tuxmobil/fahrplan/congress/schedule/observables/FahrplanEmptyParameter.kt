package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel

/**
 * Payload of the observable [fahrplanEmptyParameter][FahrplanViewModel.fahrplanEmptyParameter]
 * property in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 */
data class FahrplanEmptyParameter(

    val scheduleVersion: String

)
