package nerd.tuxmobil.fahrplan.congress.alarms

import info.metadude.android.eventfahrplan.commons.temporal.Moment

sealed interface AlarmsViewEvent {
    data class OnItemClick(val sessionId: String) : AlarmsViewEvent
    data class OnDeleteItemClick(
        val sessionId: String,
        val dayIndex: Int,
        val title: String,
        val firesAt: Moment,
    ) : AlarmsViewEvent

    data object OnDeleteAllWithConfirmationClick : AlarmsViewEvent
    data object OnDeleteAllClick : AlarmsViewEvent
}
