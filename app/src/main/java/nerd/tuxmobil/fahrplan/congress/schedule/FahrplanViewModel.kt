package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.commons.livedata.SingleLiveEvent
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
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
    private val navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator,
    private val simpleSessionFormat: SimpleSessionFormat,
    private val jsonSessionFormat: JsonSessionFormat,
    private val scrollAmountCalculator: ScrollAmountCalculator

) : ViewModel() {

    private companion object {
        const val LOG_TAG = "FahrplanViewModel"
    }

    private val mutableFahrplanParameter = MutableLiveData<FahrplanParameter>()
    val fahrplanParameter: LiveData<FahrplanParameter> = mutableFahrplanParameter

    val fahrplanEmptyParameter = SingleLiveEvent<FahrplanEmptyParameter>()

    val shareSimple = SingleLiveEvent<String>()
    val shareJson = SingleLiveEvent<String>()

    private val mutableTimeTextViewParameters = MutableLiveData<List<TimeTextViewParameter>>()
    val timeTextViewParameters: LiveData<List<TimeTextViewParameter>> = mutableTimeTextViewParameters

    val scrollToCurrentSessionParameter = SingleLiveEvent<ScrollToCurrentSessionParameter>()
    val scrollToSessionParameter = SingleLiveEvent<ScrollToSessionParameter>()

    var preserveVerticalScrollPosition: Boolean = false

    init {
        updateUncanceledSessions()
    }

    private fun updateUncanceledSessions() {
        launch {
            repository.uncanceledSessionsForDayIndex.collect { scheduleData ->
                val sessions = scheduleData.allSessions
                if (sessions.isEmpty()) {
                    val scheduleVersion = repository.readMeta().version
                    if (scheduleVersion.isNotEmpty()) {
                        fahrplanEmptyParameter.postValue(FahrplanEmptyParameter(scheduleVersion))
                    } // else: Nothing to do because schedule has not been loaded yet
                } else {
                    val fahrplanParameter = scheduleData.toFahrplanParameter()
                    mutableFahrplanParameter.postValue(fahrplanParameter)
                }
            }
        }
    }

    private fun ScheduleData.toFahrplanParameter(): FahrplanParameter {
        val dayIndex = repository.readDisplayDayIndex()
        val numDays = repository.readMeta().numDays
        val dateInfos = FahrplanMisc.createDateInfos(repository.readDateInfos())
        val dayMenuEntries = if (numDays > 1) {
            navigationMenuEntriesGenerator.getDayMenuEntries(numDays, dateInfos)
        } else {
            null
        }
        return FahrplanParameter(
            scheduleData = this,
            numDays = numDays,
            dayIndex = dayIndex,
            dayMenuEntries = dayMenuEntries
        ).also {
            logging.d(LOG_TAG, "Loaded ${allSessions.size} uncanceled sessions.")
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
                    mutableTimeTextViewParameters.postValue(parameters)
                }
            }
        }
    }

    private fun List<Session>.toTimeTextViewParameters(nowMoment: Moment, normalizedBoxHeight: Int): List<TimeTextViewParameter> {
        val earliestSession = repository.loadEarliestSession()
        val firstDayStartDay = earliestSession.startTimeMoment.monthDay
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
                shareSimple.postValue(formattedSession)
            }
        }
    }

    fun shareToChaosflix(session: Session) {
        jsonSessionFormat.format(session).let { formattedSession ->
            shareJson.postValue(formattedSession)
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
                    scrollToCurrentSessionParameter.postValue(parameter)
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
                    scrollToSessionParameter.postValue(parameter)
                }
            }
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
