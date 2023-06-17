package nerd.tuxmobil.fahrplan.congress.alarms

sealed interface AlarmsState {

    object Loading : AlarmsState {
        override fun toString(): String {
            return "Loading"
        }
    }

    data class Success(
        val sessionAlarmParameters: List<SessionAlarmParameter>,
        val onItemClick: (SessionAlarmParameter) -> Unit,
        val onDeleteItemClick: (SessionAlarmParameter) -> Unit,
    ) : AlarmsState

}
