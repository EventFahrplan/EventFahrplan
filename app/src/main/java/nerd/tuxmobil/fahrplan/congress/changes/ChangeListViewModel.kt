package nerd.tuxmobil.fahrplan.congress.changes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.CancelScheduleUpdateNotification
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Loading
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeState.Success
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnFavoritesFilterToggle
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnScheduleChangesSeen
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnSessionChangeItemClick
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

class ChangeListViewModel(
    private val repository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val executionContext: ExecutionContext,
    private val sessionChangeParametersFactory: SessionChangeParametersFactory,
) : ViewModel() {

    private val mutableEffects = Channel<SessionChangeEffect>()
    val effects = mutableEffects.receiveAsFlow()

    private val mutableSessionChangeState = MutableStateFlow<SessionChangeState>(Loading)
    val sessionChangesState = mutableSessionChangeState.asStateFlow()

    init {
        val favoritesFilterEnabled = settingsRepository.settingsStream
            .map { it.isScheduleChangesFavoritesFilterEnabled }
            .distinctUntilChanged()
        combine(repository.changedSessions, favoritesFilterEnabled) { sessions, filterFavorites ->
            val filteredSessions = if (filterFavorites) sessions.filter { it.isFavorite } else sessions
            mutableSessionChangeState.value = Success(
                sessionChangeParameters = sessionChangeParametersFactory.createSessionChangeParameters(
                    sessions = filteredSessions,
                    useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled(),
                ),
                favoritesFilterEnabled = filterFavorites,
                hasUnfilteredChanges = sessions.isNotEmpty(),
            )
        }.launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: SessionChangeViewEvent) {
        when (viewEvent) {
            is OnSessionChangeItemClick -> sendEffect(NavigateToSession(viewEvent.sessionId))
            OnScheduleChangesSeen -> onScheduleChangesSeen()
            is OnFavoritesFilterToggle -> onFavoritesFilterToggle(viewEvent.enabled)
        }
    }

    private fun onFavoritesFilterToggle(enabled: Boolean) {
        settingsRepository.setScheduleChangesFavoritesFilter(enabled)
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
