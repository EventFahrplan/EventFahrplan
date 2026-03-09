package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.CancelScheduleUpdateNotification
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnScheduleChangesSeen
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

class ChangeListViewModel(
    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val sessionChangeParametersFactory: SessionChangeParametersFactory,
) : ViewModel() {

    private val mutableEffects = Channel<SessionChangeEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val mutableSessionChangeState = MutableStateFlow<SessionChangeState>(Loading)
    val sessionChangesState = mutableSessionChangeState.asStateFlow()

    init {
        repository.changedSessions
            .onEach { sessions ->
                mutableSessionChangeState.value = Success(
                    sessionChangeParametersFactory.createSessionChangeParameters(
                        sessions = sessions,
                        useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled(),
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: SessionChangeViewEvent) {
        when (viewEvent) {
            is OnSessionChangeItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
            OnScheduleChangesSeen -> onScheduleChangesSeen()
        }
    }

    private fun onScheduleChangesSeen() {
        launch {
            repository.updateScheduleChangesSeen(true)
        }
        sendEffect(CancelScheduleUpdateNotification)
    }

    private fun sendEffect(effect: SessionChangeEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
