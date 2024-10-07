package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

class ChangeListViewModel(
    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val sessionChangeParametersFactory: SessionChangeParametersFactory,
) : ViewModel() {

    var screenNavigation: ScreenNavigation? = null

    private val mutableSessionChangeState = MutableStateFlow<SessionChangeState>(Loading)
    val sessionChangesState = mutableSessionChangeState.asStateFlow()

    init {
        repository.changedSessions
            .onEach { sessions ->
                mutableSessionChangeState.value = Success(
                    sessionChangeParametersFactory.createSessionChangeParameters(
                        sessions = sessions,
                        numDays = if (sessions.isEmpty()) 0 else repository.readMeta().numDays,
                        useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled(),
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    private val mutableScheduleChangesSeen = Channel<Unit>()
    val scheduleChangesSeen = mutableScheduleChangesSeen.receiveAsFlow()

    fun onViewEvent(viewEvent: SessionChangeViewEvent) {
        when (viewEvent) {
            is OnSessionChangeItemClick -> screenNavigation?.navigateToSessionDetails(viewEvent.sessionId)
        }
    }

    fun updateScheduleChangesSeen(changesSeen: Boolean) {
        launch {
            mutableScheduleChangesSeen.sendOneTimeEvent(Unit)
            repository.updateScheduleChangesSeen(changesSeen)
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
