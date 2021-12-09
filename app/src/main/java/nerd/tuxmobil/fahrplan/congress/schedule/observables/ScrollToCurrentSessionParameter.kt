package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel

/**
 * Payload of the observable [scrollToCurrentSessionParameter][FahrplanViewModel.scrollToCurrentSessionParameter]
 * property in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 */
data class ScrollToCurrentSessionParameter(

    val scheduleData: ScheduleData,
    val dateInfos: DateInfos

)
