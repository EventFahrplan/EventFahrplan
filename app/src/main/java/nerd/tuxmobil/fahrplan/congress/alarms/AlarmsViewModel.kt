package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsDestination.ConfirmDeleteAll
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteItemClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

internal class AlarmsViewModel(
    private val repository: AppRepository = AppRepository,
    private val executionContext: ExecutionContext,
    private val alarmServices: AlarmServices,
    private val alarmsStateFactory: AlarmsStateFactory,
) : ViewModel() {

    private val mutableEffects = Channel<AlarmsEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val useDeviceTimeZone: Boolean
        get() = repository.readUseDeviceTimeZoneEnabled()

    private val mutableAlarmsState = MutableStateFlow<AlarmsState>(Loading)
    val alarmsState = mutableAlarmsState.asStateFlow()

    val hasAlarms = alarmsState
        .filterIsInstance<Success>()
        .map { it.sessionAlarmParameters.isNotEmpty() }

    init {
        combine(repository.alarms, repository.sessions) { alarms, sessions ->
            mutableAlarmsState.value = Success(
                sessionAlarmParameters = alarmsStateFactory
                    .createAlarmsState(alarms, sessions, useDeviceTimeZone),
            )
        }.launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: AlarmsViewEvent) {
        when (viewEvent) {
            is OnItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
            is OnDeleteItemClick -> deleteSessionAlarm(viewEvent)
            OnDeleteAllClick -> deleteAllAlarms()
            OnDeleteAllWithConfirmationClick -> navigateTo(ConfirmDeleteAll)
        }
    }

    private fun deleteSessionAlarm(viewEvent: OnDeleteItemClick) {
        launch {
            repository.deleteAlarmForSessionId(viewEvent.sessionId)
            val alarm = SchedulableAlarm(
                dayIndex = viewEvent.dayIndex,
                sessionId = viewEvent.sessionId,
                sessionTitle = viewEvent.title,
                startTime = viewEvent.firesAt,
            )
            alarmServices.discardSessionAlarm(alarm)
        }
    }

    private fun deleteAllAlarms() {
        launch {
            val alarms = repository.readAlarms()
            alarms
                .map { it.toSchedulableAlarm() }
                .forEach { alarmServices.discardSessionAlarm(it) }
            if (alarms.isNotEmpty()) {
                repository.deleteAllAlarms()
            }
        }
    }

    private fun navigateTo(destination: AlarmsDestination) {
        sendEffect(NavigateTo(destination))
    }

    private fun sendEffect(effect: AlarmsEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
