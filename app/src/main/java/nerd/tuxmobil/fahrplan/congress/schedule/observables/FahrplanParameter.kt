package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel

/**
 * Payload of the observable [fahrplanParameter][FahrplanViewModel.fahrplanParameter] property
 * in the [FahrplanViewModel] which is observed by the [FahrplanFragment].
 */
data class FahrplanParameter(

    val scheduleData: ScheduleData,
    val useDeviceTimeZone: Boolean,
    val numDays: Int,
    val dayIndex: Int,
    val dayMenuEntries: List<String>

)
