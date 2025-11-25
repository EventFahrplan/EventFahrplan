package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewModel

/**
 * Payload of the observable [scheduleChangesParameter][MainViewModel.scheduleChangesParameter]
 * property in the [MainViewModel] which is observed by the [MainActivity].
 */
data class ScheduleChangesParameter(

    val scheduleVersion: String,
    val changeStatistic: ChangeStatistic

)
