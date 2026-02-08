package nerd.tuxmobil.fahrplan.congress.schedulestatistic

sealed interface ScheduleStatisticEffect {
    data object NavigateBack : ScheduleStatisticEffect
}
