package nerd.tuxmobil.fahrplan.congress.schedule.observables

data class DayMenuParameter(
    val dayMenuEntries: List<String> = emptyList(),
    val displayDayIndex: Int = 0,
) {
    val isValid = displayDayIndex > 0
}
