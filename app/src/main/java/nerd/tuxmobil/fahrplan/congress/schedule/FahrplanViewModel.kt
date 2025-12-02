package nerd.tuxmobil.fahrplan.congress.schedule

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.SessionAlarmViewModelDelegate
import nerd.tuxmobil.fahrplan.congress.dataconverters.toNumDays
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Known
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Unknown
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.TitledMessage
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.schedule.observables.DayMenuParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToCurrentSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.ScrollToSessionParameter
import nerd.tuxmobil.fahrplan.congress.schedule.observables.TimeTextViewParameter
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc

internal class FahrplanViewModel(

    private val repository: AppRepository,
    private val executionContext: ExecutionContext,
    private val logging: Logging,
    private val errorMessageFactory: ErrorMessage.Factory,
    alarmServices: AlarmServices,
    notificationHelper: NotificationHelper,
    private val navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val scrollAmountCalculator: ScrollAmountCalculator,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String,
    runsAtLeastOnAndroidTiramisu: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

) : ViewModel() {

    private companion object {
        const val LOG_TAG = "FahrplanViewModel"
        const val PROGRESS_BAR_HIDING_DELAY = 100L
    }

    private var sessionAlarmViewModelDelegate: SessionAlarmViewModelDelegate =
        SessionAlarmViewModelDelegate(
            viewModelScope,
            notificationHelper,
            alarmServices,
            runsAtLeastOnAndroidTiramisu,
        )

    private val mutableDayMenuParameter = MutableStateFlow(DayMenuParameter())
    val dayMenuParameter = mutableDayMenuParameter.asStateFlow().filter { it.isValid }

    private val mutableFahrplanParameter = MutableStateFlow<FahrplanParameter?>(null)
    val fahrplanParameter = mutableFahrplanParameter.asStateFlow().filterNotNull()

    private val mutableErrorMessage = MutableStateFlow<TitledMessage?>(null)
    val errorMessage = mutableErrorMessage.asStateFlow()

    private val mutableShowHorizontalScrollingProgressLine = Channel<Boolean>()
    val showHorizontalScrollingProgressLine = mutableShowHorizontalScrollingProgressLine.receiveAsFlow()

    private val mutableActivateScheduleUpdateAlarm = Channel<ConferenceTimeFrame>()
    val activateScheduleUpdateAlarm = mutableActivateScheduleUpdateAlarm.receiveAsFlow()

    private val mutableShareSimple = Channel<String>()
    val shareSimple = mutableShareSimple.receiveAsFlow()

    private val mutableShareJson = Channel<String>()
    val shareJson = mutableShareJson.receiveAsFlow()

    private val mutableTimeTextViewParameters = MutableStateFlow<List<TimeTextViewParameter>>(emptyList())
    val timeTextViewParameters: Flow<List<TimeTextViewParameter>> = mutableTimeTextViewParameters

    private val mutableScrollToCurrentSessionParameter = Channel<ScrollToCurrentSessionParameter>()
    val scrollToCurrentSessionParameter = mutableScrollToCurrentSessionParameter.receiveAsFlow()

    private val mutableScrollToSessionParameter = Channel<ScrollToSessionParameter>()
    val scrollToSessionParameter = mutableScrollToSessionParameter.receiveAsFlow()

    val requestPostNotificationsPermission = sessionAlarmViewModelDelegate
        .requestPostNotificationsPermission

    val notificationsDisabled = sessionAlarmViewModelDelegate
        .notificationsDisabled

    val requestScheduleExactAlarmsPermission = sessionAlarmViewModelDelegate
        .requestScheduleExactAlarmsPermission

    val showAlarmTimePicker = sessionAlarmViewModelDelegate
        .showAlarmTimePicker

    var preserveVerticalScrollPosition: Boolean = false

    init {
        updateDayMenu()
        updateSchedule()
        requestScheduleUpdateAlarm()
        updateHorizontalScrollingProgressLineVisibility()
    }

    private fun updateDayMenu() {
        launch {
            repository.sessionsWithoutShifts.filterNotNull().collectLatest { sessions ->
                val displayDayIndex = repository.readDisplayDayIndex()
                val numDays = sessions.toNumDays()
                val dayMenuEntries = if (numDays > 1) {
                    navigationMenuEntriesGenerator.getDayMenuEntries(numDays, sessions)
                } else {
                    emptyList()
                }
                mutableDayMenuParameter.value = DayMenuParameter(dayMenuEntries, displayDayIndex)
            }
        }
    }

    private fun updateSchedule() {
        launch {
            repository.uncanceledSessionsForDayIndex.collectLatest { scheduleData ->
                if (scheduleData.allSessions.isEmpty()) {
                    val scheduleVersion = repository.readMeta().version
                    if (scheduleVersion.isNotEmpty()) {
                        val errorMessage = errorMessageFactory.getMessageForEmptySchedule(scheduleVersion)
                        mutableErrorMessage.value = errorMessage
                    } // else: Nothing to do because schedule has not been loaded yet
                } else {
                    val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
                    val customizedScheduleData = scheduleData.customizeEngelsystemRoomName()
                    val parameter = FahrplanParameter(customizedScheduleData, useDeviceTimeZone)
                    logging.d(LOG_TAG, "Loaded ${parameter.scheduleData.allSessions.size} uncanceled sessions.")
                    mutableFahrplanParameter.value = parameter
                }
            }
        }
    }

    private fun updateHorizontalScrollingProgressLineVisibility() {
        launch {
            repository.loadScheduleState.collectLatest { state ->
                val shouldShow = state.toShowHorizontalScrollingProgressLine()
                if (shouldShow) {
                    delay(PROGRESS_BAR_HIDING_DELAY)
                }
                mutableShowHorizontalScrollingProgressLine.sendOneTimeEvent(shouldShow)
            }
        }
    }

    private fun LoadScheduleState.toShowHorizontalScrollingProgressLine() = when (this) {
        InitialFetching, Fetching, InitialParsing, Parsing -> false // hide while MainActivity#progressBar is still animating
        else -> true
    }

    /**
     * Observes all sessions of all days to check if the overall conference time frame changed.
     * Actives a schedule update alarm when:
     * - no sessions are present
     * - sessions are emitted for the first time
     * - the earliest start time of all sessions changed
     * - the latest end time of all session changed
     *
     * Keep code in sync with [AppRepository.loadConferenceTimeFrame]!
     */
    private fun requestScheduleUpdateAlarm() {
        launch {
            repository
                .sessions
                .map { if (it.isEmpty()) null else Conference.ofSessions(it).timeFrame }
                .distinctUntilChanged()
                .map { if (it == null) Unknown else Known(it.start, it.endInclusive) }
                .collectLatest { mutableActivateScheduleUpdateAlarm.sendOneTimeEvent(it) }
        }
    }

    /**
     * Rewrites properties to which "Engelshifts" has been applied before
     * in ShiftExtensions -> Shift.toSessionAppModel.
     */
    private fun ScheduleData.customizeEngelsystemRoomName() = copy(
        roomDataList = roomDataList.map { roomData ->
            val customRoomName = if (roomData.roomName == defaultEngelsystemRoomName) {
                customEngelsystemRoomName
            } else {
                roomData.roomName
            }
            val customSessions = roomData.sessions.map { session ->
                val customTrackName = if (session.track == defaultEngelsystemRoomName) {
                    customEngelsystemRoomName
                } else {
                    session.track
                }
                session.copy(track = customTrackName)
            }
            roomData.copy(roomName = customRoomName, sessions = customSessions)
        }
    )

    /**
     * Requests loading the schedule from the [AppRepository] to update the UI. UI components must
     * observe the respective properties exposed by the [AppRepository] to receive schedule updates.
     * The [isUserRequest] flag must be set to `true` if the requests originates from a manual
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

    /**
     * Requests loading the schedule from the [AppRepository] if [automatic schedule updates]
     * [AppRepository.readAutoUpdateEnabled] are enabled. Also see: [requestScheduleUpdate].
     */
    fun requestScheduleAutoUpdate() {
        launch {
            if (repository.readAutoUpdateEnabled()) {
                requestScheduleUpdate(isUserRequest = false)
            }
        }
    }

    fun fillTimes(nowMoment: Moment, normalizedBoxHeight: Int) {
        launch {
            repository.uncanceledSessionsForDayIndex.collectLatest { scheduleData ->
                val sessions = scheduleData.allSessions
                if (sessions.isNotEmpty()) {
                    val parameters = sessions.toTimeTextViewParameters(nowMoment, normalizedBoxHeight)
                    mutableTimeTextViewParameters.value = parameters
                }
            }
        }
    }

    private fun List<Session>.toTimeTextViewParameters(nowMoment: Moment, normalizedBoxHeight: Int): List<TimeTextViewParameter> {
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
        val conference = Conference.ofSessions(this)
        return TimeTextViewParameter.parametersOf(
            nowMoment = nowMoment,
            conference = conference,
            normalizedBoxHeight = normalizedBoxHeight,
            useDeviceTimeZone = useDeviceTimeZone
        )
    }

    fun updateFavorStatus(session: Session) {
        launch {
            repository.updateHighlight(session)
        }
    }

    fun canAddAlarms(): Boolean {
        return sessionAlarmViewModelDelegate.canAddAlarms()
    }

    fun addAlarmWithChecks() {
        sessionAlarmViewModelDelegate.addAlarmWithChecks()
    }

    fun addAlarm(session: Session, alarmTime: Int) {
        launch {
            sessionAlarmViewModelDelegate.addAlarm(session, alarmTime)
        }
    }

    fun deleteAlarm(session: Session) {
        launch {
            sessionAlarmViewModelDelegate.deleteAlarm(session)
        }
    }

    fun share(session: Session) {
        launch {
            val timeZoneId = repository.readMeta().timeZoneId
            simpleSessionFormat.format(session, timeZoneId).let { formattedSession ->
                mutableShareSimple.sendOneTimeEvent(formattedSession)
            }
        }
    }

    fun shareToChaosflix(session: Session) {
        jsonSessionFormat.format(session).let { formattedSession ->
            mutableShareJson.sendOneTimeEvent(formattedSession)
        }
    }

    fun selectDay(dayItemPosition: Int) {
        launch {
            val dayIndex = repository.readDisplayDayIndex()
            if (dayItemPosition + 1 != dayIndex) {
                saveSelectedDayIndex(dayItemPosition + 1)
                preserveVerticalScrollPosition = false
            }
        }
    }

    fun saveSelectedDayIndex(dayIndex: Int) {
        launch {
            repository.updateDisplayDayIndex(dayIndex)
        }
    }

    fun scrollToCurrentSession() {
        launch {
            val scheduleData = repository.loadUncanceledSessionsForDayIndex()
            val sessions = scheduleData.allSessions
            if (sessions.isNotEmpty()) {
                val dateInfos = FahrplanMisc.createDateInfos(repository.readDateInfos())
                if (scheduleData.dayIndex == dateInfos.indexOfToday) {
                    val parameter = ScrollToCurrentSessionParameter(scheduleData, dateInfos)
                    mutableScrollToCurrentSessionParameter.sendOneTimeEvent(parameter)
                }
            }
        }
    }

    fun scrollToSession(sessionId: String, boxHeight: Int) {
        launch {
            val scheduleData = repository.loadUncanceledSessionsForDayIndex()
            val sessions = scheduleData.allSessions
            if (sessions.isNotEmpty()) {
                val session = scheduleData.findSession(sessionId)
                if (session != null) {
                    val conference = Conference.ofSessions(sessions)
                    val verticalPosition = scrollAmountCalculator.calculateScrollAmount(conference, session, boxHeight)
                    val roomIndex = scheduleData.findRoomIndex(session)
                    val parameter = ScrollToSessionParameter(
                        sessionId = sessionId,
                        verticalPosition = verticalPosition,
                        roomIndex = roomIndex
                    )
                    mutableScrollToSessionParameter.sendOneTimeEvent(parameter)
                }
            }
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
