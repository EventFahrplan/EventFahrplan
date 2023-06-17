package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
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

internal class MainViewModel(

    private val repository: AppRepository,
    private val notificationHelper: NotificationHelper,
    private val executionContext: ExecutionContext,
    private val logging: Logging

) : ViewModel() {

    private val mutableLoadScheduleUiState = Channel<LoadScheduleUiState>()
    val loadScheduleUiState = mutableLoadScheduleUiState.receiveAsFlow()

    private val mutableFetchFailure = Channel<FetchFailure?>()
    val fetchFailure = mutableFetchFailure.receiveAsFlow()

    private val mutableParseFailure = Channel<ParseResult?>()
    val parseFailure = mutableParseFailure.receiveAsFlow()

    private val mutableScheduleChangesParameter = Channel<ScheduleChangesParameter>()
    val scheduleChangesParameter = mutableScheduleChangesParameter.receiveAsFlow()

    private val mutableShowAbout = Channel<Meta>()
    val showAbout = mutableShowAbout.receiveAsFlow()

    private val mutableOpenSessionDetails = Channel<Unit>()
    val openSessionDetails = mutableOpenSessionDetails.receiveAsFlow()

    private val mutableMissingPostNotificationsPermission = Channel<Unit>()
    val missingPostNotificationsPermission = mutableMissingPostNotificationsPermission.receiveAsFlow()

    init {
        observeLoadScheduleState()
    }

    private fun observeLoadScheduleState() {
        launch {
            repository.loadScheduleState.collect { state ->
                val uiState = state.toUiState()
                mutableLoadScheduleUiState.sendOneTimeEvent(uiState)
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
                mutableFetchFailure.sendOneTimeEvent(this)
            } else {
                // Don't bother the user with schedule up-to-date messages.
            }
        }
        is ParseFailure -> {
            mutableParseFailure.sendOneTimeEvent(parseResult)
        }
        else -> {
            mutableFetchFailure.sendOneTimeEvent(null)
            mutableParseFailure.sendOneTimeEvent(null)
        }
    }

    private fun onParsingDone() {
        if (!repository.readScheduleChangesSeen()) {
            val scheduleVersion = repository.readMeta().version
            val sessions = repository.loadChangedSessions()
            val statistic = ChangeStatistic.of(sessions, logging)
            val parameter = ScheduleChangesParameter(scheduleVersion, statistic)
            mutableScheduleChangesParameter.sendOneTimeEvent(parameter)
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
            mutableShowAbout.sendOneTimeEvent(meta)
        }
    }

    fun checkPostNotificationsPermission() {
        if (repository.readAlarms().isNotEmpty() && !notificationHelper.notificationsEnabled) {
            mutableMissingPostNotificationsPermission.sendOneTimeEvent(Unit)
        }
    }

    fun openSessionDetails(sessionId: String) {
        launch {
            val isUpdated = repository.updateSelectedSessionId(sessionId)
            if (isUpdated) {
                mutableOpenSessionDetails.sendOneTimeEvent(Unit)
            }
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
