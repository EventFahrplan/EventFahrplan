package nerd.tuxmobil.fahrplan.congress.alarms

import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success

/**
 * Payload of observable [Success] state exposed via the [alarmsState][AlarmsViewModel.alarmsState]
 * property in the [AlarmsViewModel] which is observed by the [AlarmsFragment].
 */
data class SessionAlarmParameter(
    val sessionId: String,
    val title: String,
    val titleContentDescription: String,
    val subtitle: String,
    val subtitleContentDescription: String,
    val alarmOffsetInMin: Int,
    val alarmOffsetContentDescription: String,
    val firesAt: Long,
    val firesAtText: String,
    val firesAtContentDescription: String,
    val dayIndex: Int,
    val selected: Boolean = false
)
