package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import info.metadude.android.eventfahrplan.commons.extensions.onFailure
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.RealAlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.RealHighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.RealMetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.RealSessionsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper
import info.metadude.android.eventfahrplan.engelsystem.EngelsystemNetworkRepository
import info.metadude.android.eventfahrplan.engelsystem.RealEngelsystemNetworkRepository
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.repositories.RealScheduleNetworkRepository
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import info.metadude.kotlin.library.engelsystem.models.Shift
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.dataconverters.cropToDayRangesExtent
import nerd.tuxmobil.fahrplan.congress.dataconverters.sanitize
import nerd.tuxmobil.fahrplan.congress.dataconverters.shiftRoomIndicesOfMainSchedule
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAlarmDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAlarmsAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAppFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.dataconverters.toDateInfos
import nerd.tuxmobil.fahrplan.congress.dataconverters.toDayIndices
import nerd.tuxmobil.fahrplan.congress.dataconverters.toDayRanges
import nerd.tuxmobil.fahrplan.congress.dataconverters.toHighlightDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toHighlightsAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModels
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel2
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.exceptions.AppExceptionHandler
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult
import nerd.tuxmobil.fahrplan.congress.preferences.AlarmTonePreference
import nerd.tuxmobil.fahrplan.congress.preferences.RealSharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.Companion.computeSessionsWithChangeFlags
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import okhttp3.OkHttpClient

object AppRepository {

    /**
     * Name used as the display title for the Engelsystem column and
     * for the database column. Do not change it!
     * Also used in app/src/<flavor>/res/xml/track_resource_names.xml.
     */
    const val ENGELSYSTEM_ROOM_NAME = "Engelshifts"
    private const val ALL_DAYS = -1

    private const val LOG_TAG = "AppRepository"
    private lateinit var logging: Logging

    private val parentJobs = mutableMapOf<String, Job>()
    private lateinit var executionContext: ExecutionContext
    private lateinit var databaseScope: DatabaseScope
    private lateinit var networkScope: NetworkScope

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var alarmsDatabaseRepository: AlarmsDatabaseRepository
    private lateinit var highlightsDatabaseRepository: HighlightsDatabaseRepository
    private lateinit var sessionsDatabaseRepository: SessionsDatabaseRepository
    private lateinit var metaDatabaseRepository: MetaDatabaseRepository

    private lateinit var scheduleNetworkRepository: ScheduleNetworkRepository
    private lateinit var engelsystemNetworkRepository: EngelsystemNetworkRepository
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository
    private lateinit var sessionsTransformer: SessionsTransformer

    private val mutableLoadScheduleState = MutableSharedFlow<LoadScheduleState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Emits the status on how loading (fetching and parsing) the schedule incl. Engelsystem shifts
     * works out. Only the latest emission is retained.
     */
    val loadScheduleState: Flow<LoadScheduleState> = mutableLoadScheduleState

    private val refreshStarredSessionsSignal = MutableSharedFlow<Unit>()

    private fun refreshStarredSessions() {
        logging.d(LOG_TAG, "Refreshing starred sessions ...")
        val requestIdentifier = "refreshStarredSessions"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshStarredSessionsSignal.emit(Unit)
        }
    }

    /**
     * Emits all sessions from the database which have been favored aka. starred but no canceled.
     * The returned list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val starredSessions: Flow<List<Session>> by lazy {
        refreshStarredSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadStarredSessions() }
            .flowOn(executionContext.database)
    }

    private val refreshSessionsSignal = MutableSharedFlow<Unit>()

    private fun refreshSessions() {
        logging.d(LOG_TAG, "Refreshing sessions ...")
        val requestIdentifier = "refreshSessions"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshSessionsSignal.emit(Unit)
        }
    }

    /**
     * Emits all sessions from the database..
     * The returned list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: Flow<List<Session>> by lazy {
        refreshSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadSessionsForAllDays() }
            .flowOn(executionContext.database)
    }

    private val refreshChangedSessionsSignal = MutableSharedFlow<Unit>()

    private fun refreshChangedSessions() {
        logging.d(LOG_TAG, "Refreshing changed sessions ...")
        val requestIdentifier = "refreshChangedSessions"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshChangedSessionsSignal.emit(Unit)
        }
    }

    /**
     * Emits all sessions from the database which have been marked as changed, cancelled or new.
     * The returned list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val changedSessions: Flow<List<Session>> by lazy {
        refreshChangedSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadChangedSessions() }
            .flowOn(executionContext.database)
    }

    private val refreshUncanceledSessionsSignal = MutableSharedFlow<Unit>()

    private fun refreshUncanceledSessions() {
        logging.d(LOG_TAG, "Refreshing uncanceled sessions ...")
        val requestIdentifier = "refreshUncanceledSessions"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshUncanceledSessionsSignal.emit(Unit)
        }
    }

    /**
     * Emits [ScheduleData] containing all uncanceled sessions for the currently configured day
     * from the database. The contained sessions list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uncanceledSessionsForDayIndex: Flow<ScheduleData> by lazy {
        refreshUncanceledSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadUncanceledSessionsForDayIndex() }
            // Don't use distinctUntilChanged() here unless Session highlight and hasAlarm are
            // part of equals and hashcode. Otherwise the schedule screen does not update.
            .flowOn(executionContext.database)
    }

    private val refreshSelectedSessionSignal = MutableSharedFlow<Unit>()

    private fun refreshSelectedSession() {
        logging.d(LOG_TAG, "Refreshing selected session ...")
        val requestIdentifier = "refreshSelectedSession"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshSelectedSessionSignal.emit(Unit)
        }
    }

    /**
     * Emits all sessions from the database which have been favored aka. starred but no canceled.
     * The returned list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSession: Flow<Session> by lazy {
        refreshSelectedSessionSignal
            .onStart { emit(Unit) }
            .mapLatest { loadSelectedSession() }
            .flowOn(executionContext.database)
    }

    private val refreshAlarmsSignal = MutableSharedFlow<Unit>()

    private fun refreshAlarms() {
        logging.d(LOG_TAG, "Refreshing alarms ...")
        val requestIdentifier = "refreshAlarms"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshAlarmsSignal.emit(Unit)
        }
    }

    /**
     * Emits all alarms from the database
     * The contained sessions list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val alarms: Flow<List<Alarm>> by lazy {
        refreshAlarmsSignal
            .onStart { emit(Unit) }
            .mapLatest { readAlarms() }
            .flowOn(executionContext.database)
    }

    @JvmOverloads
    fun initialize(
            context: Context,
            logging: Logging,
            executionContext: ExecutionContext = AppExecutionContext,
            databaseScope: DatabaseScope = DatabaseScope.of(executionContext, AppExceptionHandler(logging)),
            networkScope: NetworkScope = NetworkScope.of(executionContext, AppExceptionHandler(logging)),
            okHttpClient: OkHttpClient = CustomHttpClient.createHttpClient(context),
            alarmsDatabaseRepository: AlarmsDatabaseRepository = RealAlarmsDatabaseRepository(AlarmsDBOpenHelper(context), logging),
            highlightsDatabaseRepository: HighlightsDatabaseRepository = RealHighlightsDatabaseRepository(HighlightDBOpenHelper(context)),
            sessionsDatabaseRepository: SessionsDatabaseRepository = RealSessionsDatabaseRepository(SessionsDBOpenHelper(context), logging),
            metaDatabaseRepository: MetaDatabaseRepository = RealMetaDatabaseRepository(MetaDBOpenHelper(context)),
            scheduleNetworkRepository: ScheduleNetworkRepository = RealScheduleNetworkRepository(logging),
            engelsystemNetworkRepository: EngelsystemNetworkRepository = RealEngelsystemNetworkRepository(),
            sharedPreferencesRepository: SharedPreferencesRepository = RealSharedPreferencesRepository(context),
            sessionsTransformer: SessionsTransformer = SessionsTransformer.createSessionsTransformer()
    ) {
        this.logging = logging
        this.executionContext = executionContext
        this.databaseScope = databaseScope
        this.networkScope = networkScope
        this.okHttpClient = okHttpClient
        this.alarmsDatabaseRepository = alarmsDatabaseRepository
        this.highlightsDatabaseRepository = highlightsDatabaseRepository
        this.sessionsDatabaseRepository = sessionsDatabaseRepository
        this.metaDatabaseRepository = metaDatabaseRepository
        this.scheduleNetworkRepository = scheduleNetworkRepository
        this.engelsystemNetworkRepository = engelsystemNetworkRepository
        this.sharedPreferencesRepository = sharedPreferencesRepository
        this.sessionsTransformer = sessionsTransformer
    }

    private fun loadingFailed(@Suppress("SameParameterValue") requestIdentifier: String) {
        parentJobs.remove(requestIdentifier)
    }

    fun cancelLoading() {
        parentJobs.values.forEach {
            it.cancel()
        }
        parentJobs.clear()
    }

    /**
     * Loads the schedule from the given [url]. Automated calls to this function must set the
     * [isUserRequest] parameter to `false` while call originating from a direct user interaction
     * must set the parameter to `true`.
     */
    // TODO Remove zombie callbacks when cleaning up UpdateService
    @WorkerThread
    fun loadSchedule(url: String = readScheduleUrl(),
                     isUserRequest: Boolean,
                     onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
                     onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                     onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit
    ) {
        check(onFetchingDone != {}) { "Nobody registered to receive FetchScheduleResult." }
        // Fetching
        val meta = readMeta().toMetaNetworkModel()
        val fetchingStatus = if (meta.numDays == 0) InitialFetching else Fetching
        mutableLoadScheduleState.tryEmit(fetchingStatus)
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, meta.eTag) { fetchScheduleResult ->
            val fetchResult = fetchScheduleResult.toAppFetchScheduleResult()
            val fetchResultStatus = if (fetchResult.isSuccessful) {
                FetchSuccess
            } else {
                FetchFailure(fetchResult.httpStatus, fetchResult.hostName, fetchResult.exceptionMessage, isUserRequest)
            }
            mutableLoadScheduleState.tryEmit(fetchResultStatus)
            onFetchingDone.invoke(fetchResult)

            if (fetchResult.isNotModified || fetchResult.isSuccessful) {
                updateScheduleLastFetchedAt()
            }

            if (fetchResult.isSuccessful) {
                val validMeta = meta.copy(eTag = fetchScheduleResult.eTag).validate()
                updateMeta(validMeta)
                check(onParsingDone != {}) { "Nobody registered to receive ParseScheduleResult." }
                // Parsing
                val parsingStatus = if (meta.numDays == 0) InitialParsing else Parsing
                mutableLoadScheduleState.tryEmit(parsingStatus)
                parseSchedule(
                        fetchScheduleResult.scheduleXml,
                        fetchScheduleResult.eTag,
                        onParsingDone,
                        onLoadingShiftsDone
                )
            } else if (fetchResult.isNotModified) {
                loadShifts(onLoadingShiftsDone)
            }
        }
    }

    private fun parseSchedule(scheduleXml: String,
                              eTag: String,
                              onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                              onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit) {
        scheduleNetworkRepository.parseSchedule(scheduleXml, eTag,
                onUpdateSessions = { sessions ->
                    val oldSessions = loadSessionsForAllDays(true)
                    val newSessions = sessions.toSessionsAppModel2().sanitize()
                    val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
                    if (scheduleChanges.foundNoteworthyChanges) {
                        updateScheduleChangesSeen(false)
                    }
                    updateSessions(scheduleChanges.sessionsWithChangeFlags, scheduleChanges.oldCanceledSessions)
                },
                onUpdateMeta = { meta ->
                    val validMeta = meta.validate()
                    updateMeta(validMeta)
                },
                onParsingDone = { result: Boolean, version: String ->
                    val parseResult = ParseScheduleResult(result, version)
                    val parseScheduleStatus = if (result) ParseSuccess else ParseFailure(parseResult)
                    mutableLoadScheduleState.tryEmit(parseScheduleStatus)
                    onParsingDone(parseResult)
                    loadShifts(onLoadingShiftsDone)
                })
    }

    /**
     * Loads personal shifts from the Engelsystem and joins them with the conference schedule.
     * Once loading is done (successful or not) the given [onLoadingShiftsDone] function is invoked.
     */
    private fun loadShifts(onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit) {
        @Suppress("ConstantConditionIf")
        if (!BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS) {
            return
        }
        val url = readEngelsystemShiftsUrl()
        if (url.isEmpty()) {
            logging.d(LOG_TAG, "Engelsystem shifts URL is empty.")
            deleteAllEngelsystemShiftsForAllDays()
            return
        }
        val requestIdentifier = "loadShifts"
        parentJobs[requestIdentifier] = networkScope.launchNamed(requestIdentifier) {
            suspend fun notifyLoadingShiftsDone(loadShiftsResult: LoadShiftsResult) {
                networkScope.withUiContext {
                    onLoadingShiftsDone(loadShiftsResult)
                }
            }
            when (val result = engelsystemNetworkRepository.load(okHttpClient, url)) {
                is ShiftsResult.Success -> {
                    updateShifts(result.shifts)
                    notifyLoadingShiftsDone(LoadShiftsResult.Success)
                    updateLastEngelsystemShiftsHash()
                }
                is ShiftsResult.Error -> {
                    logging.e(LOG_TAG, "ShiftsResult.Error: $result")
                    loadingFailed(requestIdentifier)
                    val loadShiftsError = LoadShiftsResult.Error(result.httpStatusCode, result.exceptionMessage)
                    mutableLoadScheduleState.tryEmit(ParseFailure(ParseShiftsResult.of(loadShiftsError)))
                    notifyLoadingShiftsDone(loadShiftsError)
                }
                is ShiftsResult.Exception -> {
                    logging.e(LOG_TAG, "ShiftsResult.Exception: ${result.throwable.message}")
                    result.throwable.printStackTrace()
                    val loadShiftsException = LoadShiftsResult.Exception(result.throwable)
                    mutableLoadScheduleState.tryEmit(ParseFailure(ParseShiftsResult.of(loadShiftsException)))
                    notifyLoadingShiftsDone(loadShiftsException)
                }
            }
        }
    }

    private fun updateLastEngelsystemShiftsHash() {
        val identifier = "updateLastEngelsystemShiftsHash"
        parentJobs[identifier] = databaseScope.launchNamed(identifier) {
            val lastShiftsHash = readLastEngelsystemShiftsHash()
            val currentShiftsHash = readEngelsystemShiftsHash()
            logging.d(LOG_TAG, "Shifts hash (OLD) = $lastShiftsHash")
            logging.d(LOG_TAG, "Shifts hash (NEW) = $currentShiftsHash")
            val shiftsChanged = currentShiftsHash != lastShiftsHash
            if (shiftsChanged) {
                updateLastEngelsystemShiftsHash(currentShiftsHash)
            }
        }
    }

    /**
     * Inserts shifts or updates the locally stored shifts. Canceled shifts are deleted.
     * Shifts which take place before or after the main conference days are omitted.
     * New [shifts] are joined with conference schedule session.
     */
    private fun updateShifts(shifts: List<Shift>) {
        if (shifts.isEmpty()) {
            return
        }
        val dayRanges = loadSessionsForAllDays(includeEngelsystemShifts = false)
                .toDayRanges()
        val oldShifts = loadEngelsystemShiftsForAllDays()
        val sessionizedShifts = shifts
                .also { logging.d(LOG_TAG, "Shifts unfiltered = ${it.size}") }
                .cropToDayRangesExtent(dayRanges)
                .also { logging.d(LOG_TAG, "Shifts filtered = ${it.size}") }
                .toSessionAppModels(logging, ENGELSYSTEM_ROOM_NAME, dayRanges)
                .sanitize()
        val shiftChanges = computeSessionsWithChangeFlags(sessionizedShifts, oldShifts)
        if (oldShifts.isEmpty() || shiftChanges.foundChanges) {
            val toBeUpdatedSessions = loadSessionsForAllDays(false) // Drop all shifts before ...
                .toMutableList()
                // Shift rooms to make space for the Engelshifts room
                .shiftRoomIndicesOfMainSchedule(sessionizedShifts.toDayIndices())
                .plus(sessionizedShifts) // ... adding them again.
                .toList()
            val toBeDeletedSessions = oldShifts
                .filter { persisted -> sessionizedShifts.none { newOrUpdated -> persisted.sessionId == newOrUpdated.sessionId } }
                .also { logging.d(LOG_TAG, "Shifts to be removed = ${it.size}") }
            updateSessions(toBeUpdatedSessions, toBeDeletedSessions)
        }
    }

    /**
     * Loads the session which has been selected at last.
     */
    @WorkerThread
    fun loadSelectedSession(): Session {
        val sessionId = readSelectedSessionId()
        return readSessionBySessionId(sessionId)
    }

    /**
     * Loads all sessions from the database including Engelsystem shifts.
     * The returned list might be empty.
     */
    @WorkerThread
    private fun loadSessionsForAllDays() = loadSessionsForAllDays(true)
        .also { logging.d(LOG_TAG, "${it.size} sessions with alarm.") }

    /**
     * Load all sessions for the currently configured day from the database which have not been
     * canceled and returns them as [ScheduleData]. The contained list of sessions might be empty.
     */
    @WorkerThread
    fun loadUncanceledSessionsForDayIndex(): ScheduleData {
        val dayIndex = readDisplayDayIndex()
        val sessions = loadUncanceledSessionsForDayIndex(dayIndex)
        return sessionsTransformer.transformSessions(dayIndex, sessions)
    }

    /**
     * Loads all sessions from the database which have not been canceled.
     * The returned list might be empty.
     */
    fun loadUncanceledSessionsForDayIndex(dayIndex: Int) = loadSessionsForDayIndex(dayIndex, true)
            .filterNot { it.changedIsCanceled }
            .also { logging.d(LOG_TAG, "${it.size} uncanceled sessions.") }

    /**
     * Loads all sessions from the database which have been favored aka. starred but no canceled.
     * The returned list might be empty.
     */
    @WorkerThread
    private fun loadStarredSessions() = loadSessionsForAllDays(true)
            .filter { it.highlight && !it.changedIsCanceled }
            .also { logging.d(LOG_TAG, "${it.size} sessions starred.") }

    /**
     * Loads all sessions from the database which have been marked as changed, cancelled or new.
     * The returned list might be empty.
     */
    @WorkerThread
    fun loadChangedSessions() = loadSessionsForAllDays(true)
            .filter { it.isChanged || it.changedIsCanceled || it.changedIsNew }
            .also { logging.d(LOG_TAG, "${it.size} sessions changed.") }

    /**
     * Loads the first session of the first day from the database.
     * Throws an exception if no session is present.
     */
    @WorkerThread
    fun loadEarliestSession() = loadSessionsForAllDays(true)
            .first()

    /**
     * Loads all Engelsystem shifts for all days from the database.
     */
    private fun loadEngelsystemShiftsForAllDays() =
        readEngelsystemShiftsOrderedByDateUtc()

    /**
     * Deletes all Engelsystem shifts for all days from the database.
     */
    private fun deleteAllEngelsystemShiftsForAllDays() {
        val toBeDeletedSessions = readEngelsystemShiftsOrderedByDateUtc()
        updateSessions(emptyList(), toBeDeletedSessions)
    }

    /**
     * Loads all sessions from the database which take place on all days.
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    private fun loadSessionsForAllDays(includeEngelsystemShifts: Boolean) =
            loadSessionsForDayIndex(ALL_DAYS, includeEngelsystemShifts)

    /**
     * Loads all sessions from the database which take place on the specified [day][dayIndex].
     * All days can be loaded if -1 is passed as the [day][dayIndex].
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    private fun loadSessionsForDayIndex(dayIndex: Int, includeEngelsystemShifts: Boolean): List<Session> {
        val sessions = if (dayIndex == ALL_DAYS) {
            logging.d(LOG_TAG, "Loading sessions for all days.")
            if (includeEngelsystemShifts) {
                readSessionsOrderedByDateUtc()
            } else {
                readSessionsOrderedByDateUtcExcludingEngelsystemShifts()
            }
        } else {
            logging.d(LOG_TAG, "Loading sessions for day $dayIndex.")
            readSessionsForDayIndexOrderedByDateUtc(dayIndex)
        }
        logging.d(LOG_TAG, "Got ${sessions.size} rows.")

        val highlights = readHighlights()
        for (highlight in highlights) {
            logging.d(LOG_TAG, "$highlight")
            for (session in sessions) {
                if (session.sessionId == "${highlight.sessionId}") {
                    session.highlight = highlight.isHighlight
                }
            }
        }

        val alarmSessionIds = readAlarmSessionIds()
        for (session in sessions) {
            session.hasAlarm = session.sessionId in alarmSessionIds
        }

        return sessions.toList()
    }

    @WorkerThread
    @JvmOverloads
    fun readAlarms(sessionId: String = "") = if (sessionId.isEmpty()) {
        alarmsDatabaseRepository.query().toAlarmsAppModel()
    } else {
        alarmsDatabaseRepository.query(sessionId).toAlarmsAppModel()
    }

    private fun readAlarmSessionIds() = readAlarms().map { it.sessionId }.toSet()

    fun deleteAlarmForAlarmId(alarmId: Int) =
            alarmsDatabaseRepository.deleteForAlarmId(alarmId).also {
                refreshAlarms()
            }

    @WorkerThread
    fun deleteAllAlarms() =
        alarmsDatabaseRepository.deleteAll().also {
            refreshAlarms()
            refreshSelectedSession()
            refreshUncanceledSessions()
        }

    @WorkerThread
    fun deleteAlarmForSessionId(sessionId: String) =
        alarmsDatabaseRepository.deleteForSessionId(sessionId).also {
            refreshAlarms()
            refreshSelectedSession()
            refreshUncanceledSessions()
        }

    @WorkerThread
    fun updateAlarm(alarm: Alarm) {
        val alarmDatabaseModel = alarm.toAlarmDatabaseModel()
        val values = alarmDatabaseModel.toContentValues()
        alarmsDatabaseRepository.update(values, alarm.sessionId)
        refreshAlarms()
        refreshSelectedSession()
        refreshUncanceledSessions()
    }

    private fun readHighlights() =
            highlightsDatabaseRepository.query().toHighlightsAppModel()

    @WorkerThread
    fun updateHighlight(session: Session) {
        val highlightDatabaseModel = session.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        highlightsDatabaseRepository.update(values, session.sessionId)
        refreshStarredSessions()
        refreshSelectedSession()
        refreshUncanceledSessions()
    }

    @WorkerThread
    fun deleteAllHighlights() {
        highlightsDatabaseRepository.deleteAll()
        refreshStarredSessions()
        refreshSelectedSession()
        refreshUncanceledSessions()
    }

    private fun readSessionBySessionId(sessionId: String): Session {
        val session = sessionsDatabaseRepository.querySessionBySessionId(sessionId).toSessionAppModel()

        val highlight = highlightsDatabaseRepository.queryBySessionId(sessionId.toInt())
        if (highlight != null) {
            session.highlight = highlight.isHighlight
        }

        val alarms = alarmsDatabaseRepository.query(sessionId)
        if (alarms.isNotEmpty()) {
            session.hasAlarm = true
        }

        return session
    }

    private fun readSessionsForDayIndexOrderedByDateUtc(dayIndex: Int) =
            sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(dayIndex).toSessionsAppModel()

    private fun readSessionsOrderedByDateUtc() =
            sessionsDatabaseRepository.querySessionsOrderedByDateUtc().toSessionsAppModel()

    private fun readSessionsOrderedByDateUtcExcludingEngelsystemShifts() =
            sessionsDatabaseRepository.querySessionsWithoutRoom(ENGELSYSTEM_ROOM_NAME).toSessionsAppModel()

    private fun readEngelsystemShiftsOrderedByDateUtc() =
        sessionsDatabaseRepository.querySessionsWithinRoom(ENGELSYSTEM_ROOM_NAME).toSessionsAppModel()

    private fun readSelectedSessionId(): String {
        val id = sharedPreferencesRepository.getSelectedSessionId()
        check(id.isNotEmpty()) { "Selected session is empty." }
        return id
    }

    @WorkerThread
    fun updateSelectedSessionId(sessionId: String): Boolean {
        val isSet = sharedPreferencesRepository.setSelectedSessionId(sessionId).onFailure {
            error("Error persisting selected session ID '$sessionId'.")
        }
        return isSet.also {
            refreshSelectedSession()
        }
    }

    private fun readLastEngelsystemShiftsHash() =
            sharedPreferencesRepository.getLastEngelsystemShiftsHash()

    private fun updateLastEngelsystemShiftsHash(hash: Int) =
            sharedPreferencesRepository.setLastEngelsystemShiftsHash(hash)

    private fun readEngelsystemShiftsHash() =
            sessionsDatabaseRepository.querySessionsWithinRoom(ENGELSYSTEM_ROOM_NAME).hashCode()

    @WorkerThread
    fun readDateInfos() =
            readSessionsOrderedByDateUtc().toDateInfos()

    private fun updateSessions(toBeUpdatedSessions: List<Session>, toBeDeletedSessions: List<Session> = emptyList()) {
        val toBeUpdatedSessionsDatabaseModel = toBeUpdatedSessions.toSessionsDatabaseModel()
        val toBeUpdated = toBeUpdatedSessionsDatabaseModel.map { it.sessionId to it.toContentValues() }
        val toBeDeleted = toBeDeletedSessions.map { it.sessionId }
        sessionsDatabaseRepository.updateSessions(toBeUpdated, toBeDeleted)
        refreshStarredSessions()
        refreshSessions()
        refreshChangedSessions()
        refreshSelectedSession()
        refreshUncanceledSessions()
    }

    /**
     * Returns a unique session alarm notification ID for the given [session ID][sessionId].
     */
    fun createSessionAlarmNotificationId(sessionId: String): Int {
        val values = sessionId.toContentValues()
        return sessionsDatabaseRepository.insertSessionId(values)
    }

    /**
     * Deletes data associated with the given session alarm [notificationId] and
     * returns a boolean value indicating the success or failure of this operation.
     */
    @WorkerThread
    fun deleteSessionAlarmNotificationId(notificationId: Int): Boolean {
        return (sessionsDatabaseRepository.deleteSessionIdByNotificationId(notificationId) > 0).onFailure {
            logging.e(LOG_TAG, "Failure deleting sessionId for notificationId = $notificationId")
        }
    }

    @WorkerThread
    fun readMeta() =
            metaDatabaseRepository.query().toMetaAppModel()

    /**
     * Updates the [Meta] information in the database.
     *
     * The [Meta.eTag] field should only be written if a network response is received
     * with a status code of HTTP 200 (OK).
     *
     * See also: [HttpStatus.HTTP_OK]
     */
    private fun updateMeta(meta: Meta) {
        val metaDatabaseModel = meta.toMetaDatabaseModel()
        val values = metaDatabaseModel.toContentValues()
        metaDatabaseRepository.insert(values)
    }

    fun readScheduleRefreshIntervalDefaultValue() =
        sharedPreferencesRepository.getScheduleRefreshIntervalDefaultValue()

    fun readScheduleRefreshInterval() =
        sharedPreferencesRepository.getScheduleRefreshInterval()

    fun readAlarmTimeIndex() =
            sharedPreferencesRepository.getAlarmTimeIndex()

    /**
     * Returns the alarm tone `Uri` or `null` for silent alarms to be used for notifications.
     */
    fun readAlarmToneUri(): Uri? {
        val alarmTone = sharedPreferencesRepository.getAlarmTone()
        return AlarmToneConversion.getNotificationIntentUri(alarmTone, AlarmTonePreference.DEFAULT_VALUE_URI)
    }

    @WorkerThread
    fun readUseDeviceTimeZoneEnabled() =
        sharedPreferencesRepository.isUseDeviceTimeZoneEnabled()

    fun readAlternativeHighlightingEnabled() =
            sharedPreferencesRepository.isAlternativeHighlightingEnabled()

    @WorkerThread
    fun readAutoUpdateEnabled() =
            sharedPreferencesRepository.isAutoUpdateEnabled()

    fun readScheduleUrl(): String {
        val alternateScheduleUrl = sharedPreferencesRepository.getAlternativeScheduleUrl()
        return alternateScheduleUrl.ifEmpty {
            BuildConfig.SCHEDULE_URL
        }
    }

    private fun readEngelsystemShiftsUrl() =
            sharedPreferencesRepository.getEngelsystemShiftsUrl()

    fun readScheduleLastFetchedAt() =
            sharedPreferencesRepository.getScheduleLastFetchedAt()

    private fun updateScheduleLastFetchedAt() = with(Moment.now()) {
        sharedPreferencesRepository.setScheduleLastFetchedAt(toMilliseconds())
    }

    @WorkerThread
    fun readScheduleChangesSeen() =
            sharedPreferencesRepository.getChangesSeen()

    @WorkerThread
    fun updateScheduleChangesSeen(changesSeen: Boolean) =
            sharedPreferencesRepository.setChangesSeen(changesSeen)

    @WorkerThread
    fun readDisplayDayIndex() =
            sharedPreferencesRepository.getDisplayDayIndex()

    @WorkerThread
    fun updateDisplayDayIndex(displayDayIndex: Int) {
        sharedPreferencesRepository.setDisplayDayIndex(displayDayIndex)
        refreshUncanceledSessions()
    }

    fun readInsistentAlarmsEnabled() =
            sharedPreferencesRepository.isInsistentAlarmsEnabled()

}
