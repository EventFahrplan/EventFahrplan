package nerd.tuxmobil.fahrplan.congress.schedule

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext
import nerd.tuxmobil.fahrplan.congress.schedule.observables.FahrplanEmptyParameter
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
    private val alarmServices: AlarmServices,
    private val notificationHelper: NotificationHelper,
    private val navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val scrollAmountCalculator: ScrollAmountCalculator,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String,
    private val runsAtLeastOnAndroidTiramisu: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

) : ViewModel() {

    private companion object {
        const val LOG_TAG = "FahrplanViewModel"
    }

    val fahrplanParameter = combine(
        repository.uncanceledSessionsForDayIndex.filter { it.allSessions.isNotEmpty() },
        repository.sessionsWithoutShifts.filterNotNull(),
        repository.alarms.filterNotNull()
    ) { scheduleDataForDayIndex, allSessionsForAllDaysWithoutShifts, alarms ->
        createFahrplanParameter(
            scheduleData = scheduleDataForDayIndex.customizeEngelsystemRoomName(),
            allSessionsForAllDaysWithoutShifts = allSessionsForAllDaysWithoutShifts,
            alarms = alarms
        )
    }

    private val mutableFahrplanEmptyParameter = Channel<FahrplanEmptyParameter>()
    val fahrplanEmptyParameter = mutableFahrplanEmptyParameter.receiveAsFlow()

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

    private val mutableRequestPostNotificationsPermission = Channel<Unit>()
    val requestPostNotificationsPermission = mutableRequestPostNotificationsPermission.receiveAsFlow()

    private val mutableNotificationsDisabled = Channel<Unit>()
    val notificationsDisabled = mutableNotificationsDisabled.receiveAsFlow()

    private val mutableShowAlarmTimePicker = Channel<Unit>()
    val showAlarmTimePicker = mutableShowAlarmTimePicker.receiveAsFlow()

    var preserveVerticalScrollPosition: Boolean = false

    init {
        updateUncanceledSessions()
    }

    fun addAlarmWithChecks() {
        if (notificationHelper.notificationsEnabled) {
            mutableShowAlarmTimePicker.sendOneTimeEvent(Unit)
        } else {
            // Check runtime version here because requesting the POST_NOTIFICATION permission
            // before Android 13 (Tiramisu) has no effect nor error message.
            when (runsAtLeastOnAndroidTiramisu) {
                true -> mutableRequestPostNotificationsPermission.sendOneTimeEvent(Unit)
                false -> mutableNotificationsDisabled.sendOneTimeEvent(Unit)
            }
        }
    }

    private fun updateUncanceledSessions() {
        launch {
            repository.uncanceledSessionsForDayIndex.collect { scheduleData ->
                val sessions = scheduleData.allSessions
                if (sessions.isEmpty()) {
                    val scheduleVersion = repository.readMeta().version
                    if (scheduleVersion.isNotEmpty()) {
                        mutableFahrplanEmptyParameter.sendOneTimeEvent(FahrplanEmptyParameter(scheduleVersion))
                    } // else: Nothing to do because schedule has not been loaded yet
                }
            }
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
                Session(session).apply { track = customTrackName }
            }
            roomData.copy(roomName = customRoomName, sessions = customSessions)
        }
    )

    private fun createFahrplanParameter(
        scheduleData: ScheduleData,
        allSessionsForAllDaysWithoutShifts: List<Session>,
        alarms: List<Alarm>
    ): FahrplanParameter {
        val dayIndex = repository.readDisplayDayIndex()
        val numDays = repository.readMeta().numDays
        val dayMenuEntries = if (numDays > 1) {
            navigationMenuEntriesGenerator.getDayMenuEntries(
                numDays,
                allSessionsForAllDaysWithoutShifts
            )
        } else {
            emptyList()
        }
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()

        val scheduleDataWithAlarmFlags = createScheduleDataWithAlarmFlags(scheduleData, alarms)
        return FahrplanParameter(
            scheduleData = scheduleDataWithAlarmFlags,
            useDeviceTimeZone = useDeviceTimeZone,
            numDays = numDays,
            dayIndex = dayIndex,
            dayMenuEntries = dayMenuEntries
        ).also {
            logging.d(LOG_TAG, "Loaded ${it.scheduleData.allSessions.size} uncanceled sessions.")
        }
    }

    private fun createScheduleDataWithAlarmFlags(scheduleData: ScheduleData, alarms: List<Alarm>) =
        scheduleData.copy(roomDataList = scheduleData.roomDataList.map { roomData ->
            roomData.copy(sessions = roomData.sessions.map { session ->
                Session(session).apply {
                    hasAlarm = alarms.any { alarm ->
                        alarm.sessionId == session.sessionId
                    }
                }
            })
        })

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
            repository.uncanceledSessionsForDayIndex.collect { scheduleData ->
                val sessions = scheduleData.allSessions
                if (sessions.isNotEmpty()) {
                    val parameters = sessions.toTimeTextViewParameters(nowMoment, normalizedBoxHeight)
                    mutableTimeTextViewParameters.value = parameters
                }
            }
        }
    }

    private fun List<Session>.toTimeTextViewParameters(nowMoment: Moment, normalizedBoxHeight: Int): List<TimeTextViewParameter> {
        val earliestSession = repository.loadEarliestSession()
        val firstDayStartDay = earliestSession.startsAt.monthDay
        val useDeviceTimeZone = repository.readUseDeviceTimeZoneEnabled()
        val dayIndex = repository.readDisplayDayIndex()
        val conference = Conference.ofSessions(this)
        return TimeTextViewParameter.parametersOf(
            nowMoment,
            conference,
            firstDayStartDay,
            dayIndex,
            normalizedBoxHeight,
            useDeviceTimeZone
        )
    }

    fun updateFavorStatus(session: Session) {
        launch {
            repository.updateHighlight(session)
        }
    }

    fun addAlarm(session: Session, alarmTimesIndex: Int) {
        launch {
            alarmServices.addSessionAlarm(session, alarmTimesIndex)
        }
    }

    fun deleteAlarm(session: Session) {
        launch {
            alarmServices.deleteSessionAlarm(session)
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

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
