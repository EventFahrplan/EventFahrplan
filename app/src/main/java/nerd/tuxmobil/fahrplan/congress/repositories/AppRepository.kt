package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import info.metadude.android.eventfahrplan.commons.extensions.onFailure
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import info.metadude.android.eventfahrplan.network.repositories.RealScheduleNetworkRepository
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import info.metadude.kotlin.library.engelsystem.models.Shift
import info.metadude.kotlin.library.engelsystem.repositories.EngelsystemRepository
import info.metadude.kotlin.library.engelsystem.repositories.models.GetShiftsState
import info.metadude.kotlin.library.engelsystem.repositories.simple.SimpleEngelsystemRepository
import info.metadude.kotlin.library.roomstates.base.models.Room
import info.metadude.kotlin.library.roomstates.repositories.RoomStatesRepository
import info.metadude.kotlin.library.roomstates.repositories.simple.SimpleRoomStatesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.BuildConfig.ENABLE_FOSDEM_ROOM_STATES
import nerd.tuxmobil.fahrplan.congress.BuildConfig.FOSDEM_ROOM_STATES_PATH
import nerd.tuxmobil.fahrplan.congress.BuildConfig.FOSDEM_ROOM_STATES_URL
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
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsNetworkModel
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsRepository
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUri
import nerd.tuxmobil.fahrplan.congress.engelsystem.EngelsystemUriParser
import nerd.tuxmobil.fahrplan.congress.exceptions.AppExceptionHandler
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Known
import nerd.tuxmobil.fahrplan.congress.models.ConferenceTimeFrame.Unknown
import nerd.tuxmobil.fahrplan.congress.models.NextFetch
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
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
import nerd.tuxmobil.fahrplan.congress.schedule.Conference
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanViewModel
import nerd.tuxmobil.fahrplan.congress.search.SearchRepository
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.Companion.computeSessionsWithChangeFlags
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import okhttp3.OkHttpClient
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import info.metadude.kotlin.library.engelsystem.Api as EngelsystemApi
import info.metadude.kotlin.library.roomstates.base.Api as RoomStatesApi
import nerd.tuxmobil.fahrplan.congress.models.HttpHeader as HttpHeaderAppModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

object AppRepository : SearchRepository,
    SessionDetailsRepository {

    /**
     * Name used as the display title for the Engelsystem column and
     * for the database column. Do not change it!
     * Also used in app/src/<flavor>/res/xml/track_resource_names.xml.
     */
    const val ENGELSYSTEM_ROOM_NAME = "Engelshifts"
    private const val ALL_DAYS = -1

    /**
     * [SQLiteDatabase#insert][android.database.sqlite.SQLiteDatabase.insert]
     * returns -1 if an error occurred.
     */
    private const val DATABASE_UPDATE_ERROR = -1L

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
    private lateinit var engelsystemRepository: EngelsystemRepository
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository
    private lateinit var roomStatesRepository: RoomStatesRepository
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

    private fun refreshMeta() {
        logging.d(LOG_TAG, "Refreshing meta ...")
        val requestIdentifier = "refreshMeta"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshMetaSignal.emit(Unit)
        }
    }

    private val refreshMetaSignal = MutableSharedFlow<Unit>()

    /**
     * Emits meta from the database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val meta: Flow<MetaAppModel> by lazy {
        refreshMetaSignal
            .onStart { emit(Unit) }
            .mapLatest { readMeta() }
            .flowOn(executionContext.database)
    }

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
    val starredSessions: Flow<List<SessionAppModel>> by lazy {
        refreshStarredSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadStarredSessions().toSessionsAppModel() }
            .flowOn(executionContext.database)
    }

    private val refreshSessionsWithoutShiftsSignal = MutableSharedFlow<Unit>()

    private fun refreshSessionsWithoutShifts() {
        logging.d(LOG_TAG, "Refreshing sessions without shifts ...")
        val requestIdentifier = "refreshSessionsWithoutShifts"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshSessionsWithoutShiftsSignal.emit(Unit)
        }
    }

    /**
     * Emits all sessions excluding Engelsystem shifts from the database.
     * The returned list might be empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionsWithoutShifts: Flow<List<SessionAppModel>> by lazy {
        refreshSessionsWithoutShiftsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadSessionsForAllDays(includeEngelsystemShifts = false).toSessionsAppModel() }
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
    val sessions: Flow<List<SessionAppModel>> by lazy {
        refreshSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadSessionsForAllDays().toSessionsAppModel() }
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
    val changedSessions: Flow<List<SessionAppModel>> by lazy {
        refreshChangedSessionsSignal
            .onStart { emit(Unit) }
            .mapLatest { loadChangedSessions().toSessionsAppModel() }
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
            .distinctUntilChanged() // If server does not respond with HTTP 304 (Not modified).
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
     * Emits the session from the database which has been selected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSession: Flow<SessionAppModel> by lazy {
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

    private val refreshScheduleStatisticSignal = MutableSharedFlow<Unit>()

    private fun refreshScheduleStatistic() {
        logging.d(LOG_TAG, "Refreshing schedule statistic ...")
        val requestIdentifier = "refreshScheduleStatistic"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshScheduleStatisticSignal.emit(Unit)
        }
    }

    /**
     * Emits the statistic for each column of the schedule database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduleStatistic: Flow<List<ColumnStatistic>> by lazy {
        refreshScheduleStatisticSignal
            .onStart { emit(Unit) }
            .mapLatest { readScheduleStatistic() }
            .flowOn(executionContext.database)
    }

    private val refreshSearchHistorySignal = MutableSharedFlow<Unit>()

    private fun refreshSearchHistory() {
        logging.d(LOG_TAG, "Refreshing search history ...")
        val requestIdentifier = "refreshSearchHistory"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshSearchHistorySignal.emit(Unit)
        }
    }

    /**
     * Emits the search history from the database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchHistory: Flow<List<String>> by lazy {
        refreshSearchHistorySignal
            .onStart { emit(Unit) }
            .mapLatest { readSearchHistory() }
            .flowOn(executionContext.database)
    }

    private val refreshRoomStatesSignal = MutableSharedFlow<Unit>()

    private fun refreshRoomStates() {
        if (ENABLE_FOSDEM_ROOM_STATES) {
            logging.d(LOG_TAG, "Refreshing room states ...")
            val requestIdentifier = "refreshRoomStates"
            parentJobs[requestIdentifier] = networkScope.launchNamed(requestIdentifier) {
                refreshRoomStatesSignal.emit(Unit)
            }
        }
    }

    /**
     * Emits the room states from the live network request.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val roomStates: Flow<Result<List<Room>>> by lazy {
        refreshRoomStatesSignal
            .onStart { emit(Unit) }
            .flatMapLatest { if (ENABLE_FOSDEM_ROOM_STATES) roomStatesRepository.getRooms() else emptyFlow() }
            .flowOn(executionContext.network)
    }

    private val refreshScheduleNextFetchSignal = MutableSharedFlow<Unit>()

    private fun refreshScheduleNextFetch() {
        logging.d(LOG_TAG, "Refreshing schedule next fetch ...")
        val requestIdentifier = "refreshScheduleNextFetch"
        parentJobs[requestIdentifier] = databaseScope.launchNamed(requestIdentifier) {
            refreshScheduleNextFetchSignal.emit(Unit)
        }
    }

    /**
     * Emits the schedule [next fetch][NextFetch].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduleNextFetch: Flow<NextFetch> by lazy {
        refreshScheduleNextFetchSignal
            .onStart { emit(Unit) }
            .mapLatest { readScheduleNextFetch() }
            .flowOn(executionContext.database)
    }

    fun initialize(
            context: Context,
            logging: Logging,
            executionContext: ExecutionContext = AppExecutionContext,
            databaseScope: DatabaseScope = DatabaseScope.of(executionContext, AppExceptionHandler(logging)),
            networkScope: NetworkScope = NetworkScope.of(executionContext, AppExceptionHandler(logging)),
            okHttpClient: OkHttpClient = CustomHttpClient.createHttpClient(context),
            alarmsDatabaseRepository: AlarmsDatabaseRepository = AlarmsDatabaseRepository.get(context, logging),
            highlightsDatabaseRepository: HighlightsDatabaseRepository = HighlightsDatabaseRepository.get(context),
            sessionsDatabaseRepository: SessionsDatabaseRepository = SessionsDatabaseRepository.get(context, logging),
            metaDatabaseRepository: MetaDatabaseRepository = MetaDatabaseRepository.get(context),
            scheduleNetworkRepository: ScheduleNetworkRepository = RealScheduleNetworkRepository(logging),
            engelsystemRepository: EngelsystemRepository = SimpleEngelsystemRepository(
                callFactory = okHttpClient,
                api = EngelsystemApi,
            ),
            sharedPreferencesRepository: SharedPreferencesRepository = RealSharedPreferencesRepository(context),
            roomStatesRepository: RoomStatesRepository = SimpleRoomStatesRepository(
                url = FOSDEM_ROOM_STATES_URL,
                path = FOSDEM_ROOM_STATES_PATH,
                httpClient = okHttpClient,
                api = RoomStatesApi,
            ),
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
        this.engelsystemRepository = engelsystemRepository
        this.sharedPreferencesRepository = sharedPreferencesRepository
        this.roomStatesRepository = roomStatesRepository
        this.sessionsTransformer = sessionsTransformer
    }

    private fun loadingFailed(@Suppress("SameParameterValue") requestIdentifier: String) {
        parentJobs.remove(requestIdentifier)
    }

    fun cancelLoading() {
        val jobs = parentJobs.values.toList()
        parentJobs.clear()
        jobs.forEach(Job::cancel)
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
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, meta.httpHeader) { fetchScheduleResult ->
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
                val validMeta = meta.copy(httpHeader = fetchScheduleResult.httpHeader).validate()
                updateMeta(validMeta.toMetaDatabaseModel())
                check(onParsingDone != {}) { "Nobody registered to receive ParseScheduleResult." }
                // Parsing
                val parsingStatus = if (meta.numDays == 0) InitialParsing else Parsing
                mutableLoadScheduleState.tryEmit(parsingStatus)
                parseSchedule(
                    scheduleXml = fetchScheduleResult.scheduleXml,
                    httpHeader = fetchScheduleResult.httpHeader,
                    oldMeta = meta,
                    onParsingDone = onParsingDone,
                    onLoadingShiftsDone = onLoadingShiftsDone
                )
            } else if (fetchResult.isNotModified) {
                loadShifts(onLoadingShiftsDone)
            }
        }
    }

    private fun parseSchedule(scheduleXml: String,
                              httpHeader: HttpHeaderNetworkModel,
                              oldMeta: MetaNetworkModel,
                              onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                              onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit) {
        scheduleNetworkRepository.parseSchedule(scheduleXml, httpHeader,
                onUpdateSessions = { sessions ->
                    val oldSessions = loadSessionsForAllDays(true).toSessionsNetworkModel()
                    val newSessions = sessions.sanitize()
                    val scheduleChanges = computeSessionsWithChangeFlags(newSessions, oldSessions)
                    if (scheduleChanges.foundNoteworthyChanges) {
                        updateScheduleChangesSeen(false)
                    }
                    updateSessions(
                        scheduleChanges.sessionsWithChangeFlags.toSessionsDatabaseModel(),
                        scheduleChanges.oldCanceledSessions.toSessionsDatabaseModel(),
                    )
                },
                onUpdateMeta = { meta ->
                    val validMeta = meta.validate()
                    updateMeta(validMeta.toMetaDatabaseModel())
                },
                onParsingDone = { isSuccess: Boolean, version: String ->
                    if (!isSuccess) {
                        updateMeta(oldMeta.copy(httpHeader = HttpHeaderNetworkModel(eTag = "", lastModified = "")).toMetaDatabaseModel())
                    }
                    val parseResult = ParseScheduleResult(isSuccess, version)
                    val parseScheduleStatus = if (isSuccess) ParseSuccess else ParseFailure(parseResult)
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
        val uri = readEngelsystemShiftsUri()
        if (uri == null) {
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
            val requestHttpHeader = readEngelsystemHttpHeader()
            engelsystemRepository.getShiftsState(
                requestETag = requestHttpHeader.eTag,
                requestLastModifiedAt = requestHttpHeader.lastModified,
                baseUrl = uri.baseUrl,
                path = uri.pathPart,
                apiKey = uri.apiKey,
            ).collectLatest { state ->
                when (state) {
                    is GetShiftsState.Success -> {
                        updateShifts(state.shifts)
                        updateEngelsystemHttpHeader(HttpHeaderAppModel(eTag = state.responseETag, lastModified = state.responseLastModifiedAt))
                        notifyLoadingShiftsDone(LoadShiftsResult.Success)
                        updateLastEngelsystemShiftsHash()
                    }

                    is GetShiftsState.Error -> {
                        if (state.isNotModified) {
                            logging.d(LOG_TAG, "Error: $state")
                            loadingFailed(requestIdentifier)
                            val loadShiftsResult = LoadShiftsResult.Success
                            mutableLoadScheduleState.tryEmit(ParseSuccess)
                            notifyLoadingShiftsDone(loadShiftsResult)
                        } else {
                            logging.e(LOG_TAG, "Error: $state")
                            loadingFailed(requestIdentifier)
                            val loadShiftsError = LoadShiftsResult.Error(httpStatusCode = state.httpStatusCode, exceptionMessage = state.errorMessage)
                            mutableLoadScheduleState.tryEmit(ParseFailure(ParseShiftsResult.of(loadShiftsError)))
                            notifyLoadingShiftsDone(loadShiftsError)
                        }
                    }

                    is GetShiftsState.Failure -> {
                        logging.e(LOG_TAG, "Failure: ${state.throwable.message}")
                        state.throwable.printStackTrace()
                        val loadShiftsException = LoadShiftsResult.Exception(state.throwable)
                        mutableLoadScheduleState.tryEmit(ParseFailure(ParseShiftsResult.of(loadShiftsException)))
                        notifyLoadingShiftsDone(loadShiftsException)
                    }
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
        val oldShifts = loadEngelsystemShiftsForAllDays().toSessionsNetworkModel()
        val sessionizedShifts = shifts
                .also { logging.d(LOG_TAG, "Shifts unfiltered = ${it.size}") }
                .cropToDayRangesExtent(dayRanges)
                .also { logging.d(LOG_TAG, "Shifts filtered = ${it.size}") }
                .toSessionsNetworkModel(logging, ENGELSYSTEM_ROOM_NAME, dayRanges)
                .sanitize()
        val shiftChanges = computeSessionsWithChangeFlags(sessionizedShifts, oldShifts)
        if (oldShifts.isEmpty() || shiftChanges.foundChanges) {
            val toBeUpdatedSessions = loadSessionsForAllDays(false) // Drop all shifts before ...
                .toSessionsNetworkModel()
                .toMutableList()
                .let {
                    when {
                        // Shift rooms to make space for the Engelshifts room
                        oldShifts.isEmpty() -> it.shiftRoomIndicesOfMainSchedule(sessionizedShifts.toDayIndices())
                        // Prevent shifting room indices increasing the gap more and more
                        else -> it
                    }
                }
                .plus(sessionizedShifts) // ... adding them again.
                .toList()
            val toBeDeletedSessions = oldShifts
                .filter { persisted -> sessionizedShifts.none { newOrUpdated -> persisted.sessionId == newOrUpdated.sessionId } }
                .also { logging.d(LOG_TAG, "Shifts to be removed = ${it.size}") }
            updateSessions(
                toBeUpdatedSessions.toSessionsDatabaseModel(),
                toBeDeletedSessions.toSessionsDatabaseModel(),
            )
        }
    }

    /**
     * Loads the session which has been selected at last.
     */
    @WorkerThread
    fun loadSelectedSession(): SessionAppModel {
        val sessionId = readSelectedSessionId()
        return readSessionBySessionId(sessionId).toSessionAppModel()
    }

    /**
     * Loads the conference time frame derived from the stored session data of all days.
     *
     * Keep code in sync with [FahrplanViewModel.requestScheduleUpdateAlarm]!
     */
    fun loadConferenceTimeFrame(): ConferenceTimeFrame {
        val sessions = loadSessionsForAllDays()
        val timeFrame = if (sessions.isEmpty()) null else Conference.ofSessions(sessions.toSessionsAppModel()).timeFrame
        return if (timeFrame == null) Unknown else Known(timeFrame.start, timeFrame.endInclusive)
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
        val sessions = loadUncanceledSessionsForDayIndex(dayIndex).toSessionsAppModel()
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
            .filter { it.isHighlight && !it.changedIsCanceled }
            .also { logging.d(LOG_TAG, "${it.size} sessions starred.") }

    /**
     * Loads all sessions from the database which have been marked as changed, cancelled or new.
     * The returned list might be empty.
     */
    // TODO Stop exposing database layer model to the app layer.
    @WorkerThread
    fun loadChangedSessions(): List<SessionDatabaseModel> = loadSessionsForAllDays(true)
            .filter { it.isChanged || it.changedIsCanceled || it.changedIsNew }
            .also { logging.d(LOG_TAG, "${it.size} sessions changed.") }

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
    private fun loadSessionsForDayIndex(dayIndex: Int, includeEngelsystemShifts: Boolean): List<SessionDatabaseModel> {
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

        val highlightedSessionIds = readHighlights()
            .asSequence()
            .filter { it.isHighlight }
            .map { it.sessionId }
            .toSet()

        val highlightedSessions = sessions.map { session ->
            if (session.sessionId in highlightedSessionIds) {
                session.copy(isHighlight = true)
            } else {
                session
            }
        }

        val alarmSessionIds = readAlarmSessionIds()
        val sessionsWithAlarms = highlightedSessions.map { session ->
            if (session.sessionId in alarmSessionIds) {
                session.copy(hasAlarm = true)
            } else {
                session
            }
        }

        return sessionsWithAlarms
    }

    @WorkerThread
    fun readAlarms(sessionId: String = "") = if (sessionId.isEmpty()) {
        alarmsDatabaseRepository.query().toAlarmsAppModel()
    } else {
        alarmsDatabaseRepository.query(sessionId).toAlarmsAppModel()
    }

    private fun readAlarmSessionIds() = readAlarms().map { it.sessionId }.toSet()

    fun deleteAlarmForAlarmId(alarmId: Int) {
        if (alarmsDatabaseRepository.deleteForAlarmId(alarmId) > 0) {
            refreshAlarms()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    @WorkerThread
    fun deleteAllAlarms() {
        if (alarmsDatabaseRepository.deleteAll() > 0) {
            refreshAlarms()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    @WorkerThread
    fun deleteAlarmForSessionId(sessionId: String) {
        if (alarmsDatabaseRepository.deleteForSessionId(sessionId) > 0) {
            refreshAlarms()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    @WorkerThread
    fun updateAlarm(alarm: Alarm) {
        val alarmDatabaseModel = alarm.toAlarmDatabaseModel()
        val values = alarmDatabaseModel.toContentValues()
        if (alarmsDatabaseRepository.update(values, alarm.sessionId) != DATABASE_UPDATE_ERROR) {
            refreshAlarms()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    private fun readHighlights() =
            highlightsDatabaseRepository.query()

    @WorkerThread
    fun updateHighlight(session: SessionAppModel) {
        val highlightDatabaseModel = session.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        if (highlightsDatabaseRepository.update(values, session.sessionId) != DATABASE_UPDATE_ERROR) {
            refreshStarredSessions()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    @WorkerThread
    fun deleteHighlight(sessionId: String) {
        if (highlightsDatabaseRepository.delete(sessionId) > 0) {
            refreshStarredSessions()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    @WorkerThread
    fun deleteAllHighlights() {
        if (highlightsDatabaseRepository.deleteAll() > 0) {
            refreshStarredSessions()
            refreshSelectedSession()
            refreshRoomStates()
            refreshUncanceledSessions()
        }
    }

    private fun readSessionBySessionId(sessionId: String): SessionDatabaseModel {
        val session = sessionsDatabaseRepository
            .querySessionBySessionId(sessionId)

        val isHighlighted = highlightsDatabaseRepository
            .queryBySessionId(sessionId)
            ?.isHighlight ?: false

        val hasAlarm = alarmsDatabaseRepository
            .query(sessionId)
            .isNotEmpty()

        return if (isHighlighted || hasAlarm) {
            session.copy(
                isHighlight = isHighlighted,
                hasAlarm = hasAlarm,
            )
        } else {
            session
        }
    }

    private fun readSessionsForDayIndexOrderedByDateUtc(dayIndex: Int) =
            sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(dayIndex)

    private fun readSessionsOrderedByDateUtc() =
            sessionsDatabaseRepository.querySessionsOrderedByDateUtc()

    private fun readSessionsOrderedByDateUtcExcludingEngelsystemShifts() =
            sessionsDatabaseRepository.querySessionsWithoutRoom(ENGELSYSTEM_ROOM_NAME)

    private fun readEngelsystemShiftsOrderedByDateUtc() =
        sessionsDatabaseRepository.querySessionsWithinRoom(ENGELSYSTEM_ROOM_NAME)

    private fun readScheduleStatistic() =
        sessionsDatabaseRepository.queryScheduleStatistic()

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
            refreshRoomStates()
        }
    }

    private fun readEngelsystemHttpHeader() = HttpHeaderAppModel(
        eTag = sharedPreferencesRepository.getEngelsystemETag(),
        lastModified = sharedPreferencesRepository.getEngelsystemLastModified(),
    )

    private fun updateEngelsystemHttpHeader(httpHeader: HttpHeaderAppModel) {
        sharedPreferencesRepository.setEngelsystemETag(httpHeader.eTag)
        sharedPreferencesRepository.setEngelsystemLastModified(httpHeader.lastModified)
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

    @VisibleForTesting
    fun updateSessions(toBeUpdatedSessions: List<SessionDatabaseModel>, toBeDeletedSessions: List<SessionDatabaseModel> = emptyList()) {
        val toBeUpdated = toBeUpdatedSessions.map { it.sessionId to it.toContentValues() }
        val toBeDeleted = toBeDeletedSessions.map { it.sessionId }
        sessionsDatabaseRepository.updateSessions(toBeUpdated, toBeDeleted)
        refreshStarredSessions()
        refreshSessions()
        refreshSessionsWithoutShifts()
        refreshChangedSessions()
        refreshSelectedSession()
        refreshRoomStates()
        refreshUncanceledSessions()
        refreshScheduleStatistic()
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
     * Updates the `Meta` information in the database.
     *
     * The [MetaNetworkModel.httpHeader] properties should only be written if a
     * network response is received with a status code of HTTP 200 (OK).
     *
     * See also: [HttpStatus.HTTP_OK]
     */
    @VisibleForTesting
    fun updateMeta(meta: MetaDatabaseModel) {
        val values = meta.toContentValues()
        if (metaDatabaseRepository.insert(values) != DATABASE_UPDATE_ERROR) {
            refreshMeta()
        }
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
    override fun readUseDeviceTimeZoneEnabled() =
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

    private fun readEngelsystemShiftsUri(): EngelsystemUri? {
        val url = sharedPreferencesRepository.getEngelsystemShiftsUrl()
        return if (url.isEmpty()) null else EngelsystemUriParser().parseUri(url)
    }

    fun readScheduleLastFetchedAt() =
            sharedPreferencesRepository.getScheduleLastFetchedAt()

    private fun updateScheduleLastFetchedAt() = with(Moment.now()) {
        sharedPreferencesRepository.setScheduleLastFetchedAt(toMilliseconds())
    }

    fun readScheduleNextFetch(): NextFetch {
        return NextFetch(
            Moment.ofEpochMilli(sharedPreferencesRepository.getScheduleNextFetchAt()),
            Duration.ofMilliseconds(sharedPreferencesRepository.getScheduleNextFetchInterval()),
        )
    }

    fun updateScheduleNextFetch(nextFetch: NextFetch) {
        sharedPreferencesRepository.setScheduleNextFetchAt(nextFetch.nextFetchAt.toMilliseconds())
        sharedPreferencesRepository.setScheduleNextFetchInterval(nextFetch.interval.toWholeMilliseconds())
        refreshScheduleNextFetch()
    }

    fun deleteScheduleNextFetch() {
        sharedPreferencesRepository.resetScheduleNextFetchAt()
        sharedPreferencesRepository.resetScheduleNextFetchInterval()
        refreshScheduleNextFetch()
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
        refreshSessionsWithoutShifts()
    }

    fun readInsistentAlarmsEnabled() =
            sharedPreferencesRepository.isInsistentAlarmsEnabled()

    private fun readSearchHistory(): List<String> {
        return sharedPreferencesRepository.getSearchHistory()
    }

    override fun updateSearchHistory(history: List<String>) {
        sharedPreferencesRepository.setSearchHistory(history)
        refreshSearchHistory()
    }

}
