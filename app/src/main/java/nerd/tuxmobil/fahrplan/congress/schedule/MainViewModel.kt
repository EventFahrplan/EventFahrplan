package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticsUiState
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticsUiStateFactory
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.SimpleMessage
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.TitledMessage
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
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
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ErrorMessageUiState
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState

internal class MainViewModel(
    private val repository: AppRepository,
    private val notificationHelper: NotificationHelper,
    private val changeStatisticsUiStateFactory: ChangeStatisticsUiStateFactory,
    private val errorMessageFactory: ErrorMessage.Factory,
    private val executionContext: ExecutionContext,
) : ViewModel() {

    private val mutableLoadScheduleUiState = Channel<LoadScheduleUiState>()
    val loadScheduleUiState = mutableLoadScheduleUiState.receiveAsFlow()

    private val mutableErrorMessage = MutableStateFlow<TitledMessage?>(null)
    val errorMessage = mutableErrorMessage.asStateFlow()

    private val mutableSimpleErrorMessageUiState = Channel<ErrorMessageUiState?>()
    val simpleErrorMessageUiState = mutableSimpleErrorMessageUiState.receiveAsFlow()

    private val mutableChangeStatisticsUiState = MutableStateFlow<ChangeStatisticsUiState?>(null)
    val changeStatisticsUiState = mutableChangeStatisticsUiState.asStateFlow()

    private val mutableOpenSessionChanges = Channel<Unit>()
    val openSessionChanges = mutableOpenSessionChanges.receiveAsFlow()

    private val mutableShowAbout = Channel<Unit>()
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
            repository.loadScheduleState.collectLatest { state ->
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
                showFetchErrorMessage(httpStatus = httpStatus, hostName = hostName, exceptionMessage = exceptionMessage)
            } else {
                // Don't bother the user with schedule up-to-date messages.
            }
        }

        is ParseFailure -> {
            showParseErrorMessage(parseResult)
        }

        else -> {
            mutableErrorMessage.value = null
            mutableSimpleErrorMessageUiState.sendOneTimeEvent(null)
        }
    }

    private fun showFetchErrorMessage(httpStatus: HttpStatus, hostName: String, exceptionMessage: String) {
        if (httpStatus == HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE) {
            val message = errorMessageFactory.getCertificateMessage(exceptionMessage)
            mutableErrorMessage.value = message
        } else {
            when (val message = errorMessageFactory.getMessageForHttpStatus(httpStatus, hostName)) {
                is TitledMessage -> mutableErrorMessage.value = message
                is SimpleMessage -> mutableSimpleErrorMessageUiState.sendOneTimeEvent(ErrorMessageUiState(message, shouldShowLong = false))
            }
        }
    }

    private fun showParseErrorMessage(parseResult: ParseResult) {
        when (val message = errorMessageFactory.getMessageForParsingResult(parseResult)) {
            is TitledMessage -> mutableErrorMessage.value = message
            is SimpleMessage -> mutableSimpleErrorMessageUiState.sendOneTimeEvent(ErrorMessageUiState(message, shouldShowLong = true))
        }
    }

    private fun onParsingDone() {
        if (!repository.readScheduleChangesSeen()) {
            val changedSessions = repository.loadChangedSessions().toSessionsAppModel()
            if (changedSessions.isNotEmpty()) {
                val scheduleVersion = repository.readMeta().version
                val allSessions = repository.loadSessionsForAllDays().toSessionsAppModel()
                val state = changeStatisticsUiStateFactory.createChangeStatisticsUiState(changedSessions, allSessions.size, scheduleVersion)
                mutableChangeStatisticsUiState.value = state
            }
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
            mutableShowAbout.sendOneTimeEvent(Unit)
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

    fun onCloseChangeStatisticsScreen(shouldOpenSessionChanges: Boolean) {
        mutableChangeStatisticsUiState.value = null
        repository.updateScheduleChangesSeen(true)
        if (shouldOpenSessionChanges) {
            mutableOpenSessionChanges.sendOneTimeEvent(Unit)
        }
    }

    fun onCloseErrorMessageScreen() {
        mutableErrorMessage.value = null
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
