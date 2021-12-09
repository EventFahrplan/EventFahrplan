package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.livedata.SingleLiveEvent
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScheduleChangesParameter

class MainViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging

) : ViewModel() {

    val loadScheduleUiState = SingleLiveEvent<LoadScheduleUiState>()
    val fetchFailure = SingleLiveEvent<FetchFailure?>()
    val parseFailure = SingleLiveEvent<ParseResult?>()
    val scheduleChangesParameter = SingleLiveEvent<ScheduleChangesParameter>()

    val showAbout = SingleLiveEvent<Meta>()
    val openSessionDetails = SingleLiveEvent<Unit>()

    init {
        observeLoadScheduleState()
    }

    private fun observeLoadScheduleState() {
        launch {
            repository.loadScheduleState.collect { state ->
                val uiState = state.toUiState()
                loadScheduleUiState.postValue(uiState)
                state.handleFailureStates()
            }
        }
    }

    private fun LoadScheduleState.toUiState() = when (this) {
        InitialFetching -> LoadScheduleUiState.Initializing.InitialFetching
        Fetching -> LoadScheduleUiState.Active.Fetching
        FetchSuccess -> LoadScheduleUiState.Success.FetchSuccess
        is FetchFailure -> {
            if (isUserRequest) {
                LoadScheduleUiState.Failure.UserTriggeredFetchFailure
            } else {
                // Don't bother the user with schedule up-to-date messages.
                LoadScheduleUiState.Failure.SilentFetchFailure
            }
        }
        InitialParsing -> LoadScheduleUiState.Initializing.InitialParsing
        Parsing -> LoadScheduleUiState.Active.Parsing
        ParseSuccess -> LoadScheduleUiState.Success.ParseSuccess.also {
            onParsingDone()
        }
        is ParseFailure -> LoadScheduleUiState.Failure.ParseFailure
    }

    private fun LoadScheduleState.handleFailureStates() = when (this) {
        is FetchFailure -> {
            if (isUserRequest) {
                fetchFailure.postValue(this)
            } else {
                // Don't bother the user with schedule up-to-date messages.
            }
        }
        is ParseFailure -> {
            parseFailure.postValue(parseResult)
        }
        else -> {
            fetchFailure.postValue(null)
            parseFailure.postValue(null)
        }
    }

    private fun onParsingDone() {
        if (!repository.readScheduleChangesSeen()) {
            val scheduleVersion = repository.readMeta().version
            val sessions = repository.loadChangedSessions()
            val statistic = ChangeStatistic.of(sessions, logging)
            val parameter = ScheduleChangesParameter(scheduleVersion, statistic)
            scheduleChangesParameter.postValue(parameter)
        }
    }

    /**
     * Requests loading the schedule from the [AppRepository] to update the UI. UI components must
     * observe the respective properties exposed by the [AppRepository] to receive schedule updates.
     * The [isUserRequest] must be set to `true` if the requests originates from a manual
     * interaction of the user with the UI; otherwise `false`.
     */
    fun requestScheduleUpdate(isUserRequest: Boolean) {
        launch {
            repository.loadSchedule(
                isUserRequest = isUserRequest,
                onFetchingDone = {},
                onParsingDone = {},
                onLoadingShiftsDone = {}
            )
        }
    }

    fun cancelLoading() {
        // AppRepository wraps the call in a CoroutineScope itself.
        repository.cancelLoading()
    }

    fun deleteSessionAlarmNotificationId(notificationId: Int) {
        launch {
            repository.deleteSessionAlarmNotificationId(notificationId)
        }
    }

    fun showAboutDialog() {
        launch {
            val meta = repository.readMeta()
            showAbout.postValue(meta)
        }
    }

    fun openSessionDetails(sessionId: String) {
        launch {
            val isUpdated = repository.updateSelectedSessionId(sessionId)
            if (isUpdated) {
                openSessionDetails.postValue(Unit)
            }
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
