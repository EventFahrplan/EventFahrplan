package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel

/**
 * Payload of the observable [scrollToSessionParameter][FahrplanViewModel.scrollToSessionParameter]
 * property in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 */
data class ScrollToSessionParameter(

    val guid: String,
    val verticalPosition: Int,
    val roomIndex: Int

)
