package nerd.tuxmobil.fahrplan.congress.details

import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.SessionAlarmViewModelDelegate
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvision
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.dataconverters.toRoom
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.CloseDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.NavigateToRoom
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.OpenFeedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestPostNotificationsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestScheduleExactAlarmsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowAlarmTimePicker
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowNotificationsDisabledError
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Loading
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsState.Success
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.navigation.IndoorNavigation
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.roomstates.RoomStateFormatting
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposition

internal class SessionDetailsViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging,
    private val buildConfigProvision: BuildConfigProvision,
    alarmServices: AlarmServices,
    notificationHelper: NotificationHelper,
    private val externalNavigation: ExternalNavigation,
    private val sessionDetailsParameterFactory: SessionDetailsParameterFactory,
    private val selectedSessionParameterFactory: SelectedSessionParameterFactory,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val feedbackUrlComposition: FeedbackUrlComposition,
    private val indoorNavigation: IndoorNavigation,
    private val roomStateFormatting: RoomStateFormatting,
    runsAtLeastOnAndroidTiramisu: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

) : ViewModel(), RoomStateFormatting by roomStateFormatting {

    private companion object {
        const val LOG_TAG = "SessionDetailsViewModel"
    }

    private var sessionAlarmViewModelDelegate: SessionAlarmViewModelDelegate =
        SessionAlarmViewModelDelegate(
            viewModelScope,
            notificationHelper,
            alarmServices,
            runsAtLeastOnAndroidTiramisu,
        )

    val selectedSessionParameter: Flow<SelectedSessionParameter> = repository.selectedSession
        .map { selectedSessionParameterFactory.createSelectedSessionParameter(it) }
        .flowOn(executionContext.database)

    private val mutableSessionDetailsState = MutableStateFlow<SessionDetailsState>(Loading)
    val sessionDetailsState = mutableSessionDetailsState.asStateFlow()

    private val mutableRoomStateMessage = MutableStateFlow(roomStateFormatting.getText(null))
    val roomStateMessage = mutableRoomStateMessage.asStateFlow()

    private val mutableEffects = Channel<SessionDetailsEffect>()
    val effects = mutableEffects.receiveAsFlow()

    val showRoomState = buildConfigProvision.enableFosdemRoomStates

    init {
        if (buildConfigProvision.enableFosdemRoomStates) {
            updateRoomState()
        }
        updateSessionDetailsState()
        sendSessionAlarmEffects()
    }

    private fun sendSessionAlarmEffects() {
        sessionAlarmViewModelDelegate.showAlarmTimePicker
            .onEach { sendEffect(ShowAlarmTimePicker) }
            .launchIn(viewModelScope)

        sessionAlarmViewModelDelegate.requestPostNotificationsPermission
            .onEach { sendEffect(RequestPostNotificationsPermission) }
            .launchIn(viewModelScope)

        sessionAlarmViewModelDelegate.requestScheduleExactAlarmsPermission
            .onEach { sendEffect(RequestScheduleExactAlarmsPermission) }
            .launchIn(viewModelScope)

        sessionAlarmViewModelDelegate.notificationsDisabled
            .onEach { sendEffect(ShowNotificationsDisabledError) }
            .launchIn(viewModelScope)
    }

    fun onViewEvent(event: SessionDetailsViewEvent) {
        when (event) {
            is SessionDetailsViewEvent.OnSessionLinkClick -> openLink(event.link)
        }
    }

    private fun openLink(link: String) {
        val defaultBrowser = externalNavigation.getDefaultBrowsableApp()
        val browsers = externalNavigation.getBrowserApps().filter { it != buildConfigProvision.packageName }
        val desiredBrowser = when (defaultBrowser in browsers) {
            true -> defaultBrowser
            false -> browsers.firstOrNull()
        }
        if (desiredBrowser == null) {
            externalNavigation.openLink(link)
        } else {
            externalNavigation.openLinkWithApp(link = link, packageName = desiredBrowser)
        }
    }

    private fun updateSessionDetailsState() {
        repository.selectedSession
            .map { sessionDetailsParameterFactory.createSessionDetailsParameters(it) }
            .map { Success(it) }
            .onEach { mutableSessionDetailsState.value = it }
            .launchIn(viewModelScope)
    }

    fun openFeedback() {
        loadSelectedSession { session ->
            val uri = feedbackUrlComposition.getFeedbackUrl(session).toUri()
            sendEffect(OpenFeedback(uri))
        }
    }

    fun share() {
        loadSelectedSession { session ->
            val timeZoneId = repository.readMeta().timeZoneId
            sendEffect(ShareSimple(simpleSessionFormat.format(session, timeZoneId)))
        }
    }

    fun shareToChaosflix() {
        loadSelectedSession { session ->
            sendEffect(ShareJson(jsonSessionFormat.format(session)))
        }
    }

    fun addToCalendar() {
        loadSelectedSession { session ->
            sendEffect(AddToCalendar(session))
        }
    }

    fun favorSession() {
        loadSelectedSession { session ->
            val favoredSession = session.copy(
                isHighlight = true // Required: Update property because updateHighlight refers to its value!
            )
            repository.updateHighlight(favoredSession)
        }
    }

    fun unfavorSession() {
        loadSelectedSession { session ->
            val unfavoredSession = session.copy(
                isHighlight = false // Required: Update property because updateHighlight refers to its value!
            )
            repository.updateHighlight(unfavoredSession)
        }
    }

    fun canAddAlarms(): Boolean {
        return sessionAlarmViewModelDelegate.canAddAlarms()
    }

    fun addAlarmWithChecks() {
        sessionAlarmViewModelDelegate.addAlarmWithChecks()
    }

    fun addAlarm(alarmTime: Int) {
        loadSelectedSession { session ->
            sessionAlarmViewModelDelegate.addAlarm(session, alarmTime)
        }
    }

    fun deleteAlarm() {
        loadSelectedSession { session ->
            sessionAlarmViewModelDelegate.deleteAlarm(session)
        }
    }

    fun navigateToRoom() {
        loadSelectedSession { session ->
            val room = session.toRoom()
            val uri = indoorNavigation.getUri(room)
            sendEffect(NavigateToRoom(uri))
        }
    }

    fun closeDetails() {
        sendEffect(CloseDetails)
    }

    private fun loadSelectedSession(onSessionLoaded: (Session) -> Unit) {
        launch {
            onSessionLoaded(repository.loadSelectedSession())
        }
    }

    private fun updateRoomState() {
        combine(repository.selectedSession, repository.roomStates) { session, result ->
            result
                .onSuccess { rooms ->
                    val state = rooms.singleOrNull { it.name.trim().equals(session.roomName, ignoreCase = true) }?.state
                    if (state == null) {
                        logging.e(LOG_TAG, """Error matching room names. Unknown room name: "${session.roomName}".""")
                    }
                    mutableRoomStateMessage.value = roomStateFormatting.getText(state)
                }
                .onFailure {
                    logging.e(LOG_TAG, "Error fetching room states: $it")
                    mutableRoomStateMessage.value = roomStateFormatting.getFailureText(it)
                }
        }.launchIn(viewModelScope)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun sendEffect(effect: SessionDetailsEffect) {
        viewModelScope.launch {
            mutableEffects.send(effect)
        }
    }

}
