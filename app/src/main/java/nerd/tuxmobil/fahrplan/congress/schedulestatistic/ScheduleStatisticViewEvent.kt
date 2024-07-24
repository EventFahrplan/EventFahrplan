package nerd.tuxmobil.fahrplan.congress.schedulestatistic

sealed interface ScheduleStatisticViewEvent {
    data object OnBackClick : ScheduleStatisticViewEvent
    data object OnToggleSorting : ScheduleStatisticViewEvent
}
