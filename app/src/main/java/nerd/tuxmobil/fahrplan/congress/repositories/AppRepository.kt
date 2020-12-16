package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import info.metadude.android.eventfahrplan.commons.extensions.onFailure
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.SessionsDBOpenHelper
import info.metadude.android.eventfahrplan.engelsystem.EngelsystemNetworkRepository
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import kotlinx.coroutines.Job
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
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsNetworkModel
import nerd.tuxmobil.fahrplan.congress.exceptions.AppExceptionHandler
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.preferences.AlarmTonePreference
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges.toFlaggedSessions
import nerd.tuxmobil.fahrplan.congress.utils.AlarmToneConversion
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import okhttp3.OkHttpClient
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import info.metadude.kotlin.library.engelsystem.models.Shift as ShiftNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

object AppRepository {

    /**
     * Name used as the display title for the Engelsystem column and
     * for the database column. Do not change it!
     * Also used in app/src/<flavor>/res/xml/track_resource_names.xml.
     */
    const val ENGELSYSTEM_ROOM_NAME = "Engelshifts"
    private const val ALL_DAYS = -1

    private lateinit var context: Context

    private lateinit var logging: Logging

    private val parentJobs = mutableMapOf<String, Job>()
    private lateinit var networkScope: NetworkScope

    private lateinit var alarmsDatabaseRepository: AlarmsDatabaseRepository
    private lateinit var highlightsDatabaseRepository: HighlightsDatabaseRepository
    private lateinit var sessionsDatabaseRepository: SessionsDatabaseRepository
    private lateinit var metaDatabaseRepository: MetaDatabaseRepository

    private lateinit var scheduleNetworkRepository: ScheduleNetworkRepository
    private lateinit var engelsystemNetworkRepository: EngelsystemNetworkRepository
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    private var onSessionsChangeListener: OnSessionsChangeListener? = null
    private var alarmsHaveChanged = false
    private var highlightsHaveChanged = false

    @JvmOverloads
    fun initialize(
            context: Context,
            logging: Logging,
            networkScope: NetworkScope = NetworkScope.of(AppExecutionContext, AppExceptionHandler(logging)),
            alarmsDatabaseRepository: AlarmsDatabaseRepository = AlarmsDatabaseRepository(AlarmsDBOpenHelper(context)),
            highlightsDatabaseRepository: HighlightsDatabaseRepository = HighlightsDatabaseRepository(HighlightDBOpenHelper(context)),
            sessionsDatabaseRepository: SessionsDatabaseRepository = SessionsDatabaseRepository(SessionsDBOpenHelper(context), logging),
            metaDatabaseRepository: MetaDatabaseRepository = MetaDatabaseRepository(MetaDBOpenHelper(context)),
            scheduleNetworkRepository: ScheduleNetworkRepository = ScheduleNetworkRepository(),
            engelsystemNetworkRepository: EngelsystemNetworkRepository = EngelsystemNetworkRepository(),
            sharedPreferencesRepository: SharedPreferencesRepository = SharedPreferencesRepository(context)
    ) {
        this.context = context
        this.logging = logging
        this.networkScope = networkScope
        this.alarmsDatabaseRepository = alarmsDatabaseRepository
        this.highlightsDatabaseRepository = highlightsDatabaseRepository
        this.sessionsDatabaseRepository = sessionsDatabaseRepository
        this.metaDatabaseRepository = metaDatabaseRepository
        this.scheduleNetworkRepository = scheduleNetworkRepository
        this.engelsystemNetworkRepository = engelsystemNetworkRepository
        this.sharedPreferencesRepository = sharedPreferencesRepository
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

    fun loadSchedule(url: String,
                     okHttpClient: OkHttpClient,
                     onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
                     onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                     onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit
    ) {
        check(onFetchingDone != {}) { "Nobody registered to receive FetchScheduleResult." }
        // Fetching
        val meta = readMeta().toMetaNetworkModel()
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, meta.eTag) { fetchScheduleResult ->
            val fetchResult = fetchScheduleResult.toAppFetchScheduleResult()
            onFetchingDone.invoke(fetchResult)

            if (fetchResult.isNotModified || fetchResult.isSuccessful) {
                updateScheduleLastFetchedAt()
            }

            if (fetchResult.isSuccessful) {
                val validMeta = meta.copy(eTag = fetchScheduleResult.eTag).validate()
                updateMeta(validMeta)
                check(onParsingDone != {}) { "Nobody registered to receive ParseScheduleResult." }
                // Parsing
                parseSchedule(
                        fetchScheduleResult.scheduleXml,
                        fetchScheduleResult.eTag,
                        okHttpClient,
                        onParsingDone,
                        onLoadingShiftsDone
                )
            }
            if (fetchResult.isNotModified) {
                loadShifts(okHttpClient, onLoadingShiftsDone)
            }
        }
    }

    private fun parseSchedule(scheduleXml: String,
                              eTag: String,
                              okHttpClient: OkHttpClient,
                              onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                              onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit) {
        scheduleNetworkRepository.parseSchedule(scheduleXml, eTag,
                onUpdateSessions = { sessions ->
                    val oldSessions = readSessionsForAllDays(true)
                    val newFlaggedSessions = sessions.toFlaggedSessions(oldSessions.toSessionsNetworkModel()) {
                        Log.e(javaClass.simpleName, "hasChanges = true")
                        resetChangesSeenFlag()
                    }
                    Log.e(javaClass.simpleName, "newFlaggedSessions = ${newFlaggedSessions.size}")
                    val newSessions = newFlaggedSessions.sanitize()
                    updateSessions(newSessions)
                },
                onUpdateMeta = { meta ->
                    val validMeta = meta.validate()
                    updateMeta(validMeta)
                },
                onParsingDone = { result: Boolean, version: String ->
                    onParsingDone(ParseScheduleResult(result, version))
                    loadShifts(okHttpClient, onLoadingShiftsDone)
                })
    }

    /**
     * Loads personal shifts from the Engelsystem and joins them with the conference schedule.
     * Once loading is done (successful or not) the given [onLoadingShiftsDone] function is invoked.
     */
    private fun loadShifts(okHttpClient: OkHttpClient,
                           onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit) {
        @Suppress("ConstantConditionIf")
        if (!BuildConfig.ENABLE_ENGELSYSTEM_SHIFTS) {
            return
        }
        val url = readEngelsystemShiftsUrl()
        if (url.isEmpty()) {
            logging.d(javaClass.simpleName, "Engelsystem shifts URL is empty.")
            // TODO Cancel or remote shifts from database?
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
                }
                is ShiftsResult.Error -> {
                    logging.e(javaClass.simpleName, "ShiftsResult.Error: $result")
                    loadingFailed(requestIdentifier)
                    notifyLoadingShiftsDone(LoadShiftsResult.Error(result.httpStatusCode, result.exceptionMessage))
                }
                is ShiftsResult.Exception -> {
                    logging.e(javaClass.simpleName, "ShiftsResult.Exception: ${result.throwable.message}")
                    result.throwable.printStackTrace()
                    notifyLoadingShiftsDone(LoadShiftsResult.Exception(result.throwable))
                }
            }
        }
    }

    /**
     * Updates the locally stored shifts. Old shifts are dropped.
     * Shifts which take place before or after the main conference days are omitted.
     * New [shifts] are joined with conference schedule session.
     */
    private fun updateShifts(shifts: List<ShiftNetworkModel>) {
        if (shifts.isEmpty()) {
            return
        }
        val dayRanges = readSessionsForAllDays(includeEngelsystemShifts = false)
                .toSessionsNetworkModel()
                .toDayRanges()
        val sessionizedShifts = shifts
                .also { logging.d(javaClass.simpleName, "Shifts unfiltered = ${it.size}") }
                .cropToDayRangesExtent(dayRanges)
                .also { logging.d(javaClass.simpleName, "Shifts filtered = ${it.size}") }
                .toSessionsNetworkModel(logging, ENGELSYSTEM_ROOM_NAME, dayRanges)
                .sanitize()
        val sessions = readSessionsForAllDays(false) // Drop all shifts before ...
                .toSessionsNetworkModel()
                .toMutableList()
                // Shift rooms to make space for the Engelshifts room
                .shiftRoomIndicesOfMainSchedule(sessionizedShifts.toDayIndices())
                .plus(sessionizedShifts) // ... adding them again.
                .toList()
        // TODO Detect shift changes as it happens for sessions
        updateSessions(sessions)
    }

    /**
     * Loads all sessions from the database which have not been canceled.
     * The returned list might be empty.
     */
    fun loadUncanceledSessionsForDayIndex(dayIndex: Int) = readSessionsForDayIndex(dayIndex, true)
            .filterNot { it.changedIsCanceled }
            .toSessionsAppModel()
            .also { logging.d(javaClass.simpleName, "${it.size} uncanceled sessions.") }

    /**
     * Loads all sessions from the database which have been favored aka. starred but no canceled.
     * The returned list might be empty.
     */
    fun loadStarredSessions() = readSessionsForAllDays(true)
            .filter { it.isHighlight && !it.changedIsCanceled }
            .toSessionsAppModel()
            .also { logging.d(javaClass.simpleName, "${it.size} sessions starred.") }

    /**
     * Loads all sessions from the database which have been marked as changed, cancelled or new.
     * The returned list might be empty.
     */
    fun loadChangedSessions() = readSessionsForAllDays(true)
            .filter { it.isChanged || it.changedIsCanceled || it.changedIsNew }
            .toSessionsAppModel()
            .also { logging.d(javaClass.simpleName, "${it.size} sessions changed.") }

    /**
     * Reads all sessions from the database which take place on all days.
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    private fun readSessionsForAllDays(includeEngelsystemShifts: Boolean) =
            readSessionsForDayIndex(ALL_DAYS, includeEngelsystemShifts)

    /**
     * Reads all sessions from the database which take place on the specified [day][dayIndex].
     * All days can be loaded if -1 is passed as the [day][dayIndex].
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    private fun readSessionsForDayIndex(dayIndex: Int, includeEngelsystemShifts: Boolean): List<SessionDatabaseModel> {
        val sessions = if (dayIndex == ALL_DAYS) {
            logging.d(javaClass.simpleName, "Loading sessions for all days.")
            if (includeEngelsystemShifts) {
                readSessionsOrderedByDateUtc()
            } else {
                readSessionsOrderedByDateUtcExcludingEngelsystemShifts()
            }
        } else {
            logging.d(javaClass.simpleName, "Loading sessions for day $dayIndex.")
            readSessionsForDayIndexOrderedByDateUtc(dayIndex)
        }
        logging.d(javaClass.simpleName, "Got ${sessions.size} rows.")

        val highlights = readHighlights()
        val highlightsAwareSessions = mutableListOf<SessionDatabaseModel>()
        for (highlight in highlights) {
            logging.d(javaClass.simpleName, "$highlight")
            for (session in sessions) {
                highlightsAwareSessions += if (session.sessionId == "${highlight.sessionId}") {
                    session.copy(isHighlight = highlight.isHighlight)
                } else {
                    session
                }
            }
        }
        if (highlights.isEmpty() && highlightsAwareSessions.isEmpty()) {
            highlightsAwareSessions.addAll(sessions)
        }

        val alarmSessionIds = loadAlarmSessionIds()
        val alarmsAwareSessions = highlightsAwareSessions.map { it.copy(hasAlarm = it.sessionId in alarmSessionIds) }

        return alarmsAwareSessions.toList()
    }

    @JvmOverloads
    fun loadAlarms(sessionId: String = "") = readAlarms(sessionId).toAlarmsAppModel()

    private fun readAlarms(sessionId: String = "") = if (sessionId.isEmpty()) {
        alarmsDatabaseRepository.query()
    } else {
        alarmsDatabaseRepository.query(sessionId)
    }

    fun loadAlarmSessionIds() = readAlarms().map { it.sessionId }.toSet()

    fun deleteAlarmForAlarmId(alarmId: Int) =
            alarmsDatabaseRepository.deleteForAlarmId(alarmId)

    fun deleteAlarmForSessionId(sessionId: String) =
            alarmsDatabaseRepository.deleteForSessionId(sessionId)

    fun updateAlarm(alarm: Alarm) {
        val alarmDatabaseModel = alarm.toAlarmDatabaseModel()
        val values = alarmDatabaseModel.toContentValues()
        alarmsDatabaseRepository.update(values, alarm.sessionId)
    }

    fun loadHighlightSessionIds() = readHighlights()
            .asSequence()
            .filter { it.isHighlight }
            .map { it.sessionId.toString() }
            .toSet()

    private fun readHighlights() =
            highlightsDatabaseRepository.query()

    fun updateHighlight(session: SessionAppModel) {
        val highlightDatabaseModel = session.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        highlightsDatabaseRepository.update(values, session.sessionId)
    }

    fun deleteAllHighlights() {
        highlightsDatabaseRepository.deleteAll()
    }

    fun loadSessionBySessionId(sessionId: String): SessionAppModel {
        return readSessionBySessionId(sessionId).toSessionAppModel()
    }

    private fun readSessionBySessionId(sessionId: String): SessionDatabaseModel {
        var session = sessionsDatabaseRepository.querySessionBySessionId(sessionId)

        val highlight = highlightsDatabaseRepository.queryBySessionId(sessionId.toInt())
        if (highlight != null) {
            session = session.copy(isHighlight = highlight.isHighlight)
        }

        val alarms = alarmsDatabaseRepository.query(sessionId)
        if (alarms.isNotEmpty()) {
            session = session.copy(hasAlarm = true)
        }

        return session
    }

    private fun readSessionsForDayIndexOrderedByDateUtc(dayIndex: Int) =
            sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(dayIndex)

    private fun readSessionsOrderedByDateUtc() =
            sessionsDatabaseRepository.querySessionsOrderedByDateUtc()

    private fun readSessionsOrderedByDateUtcExcludingEngelsystemShifts() =
            sessionsDatabaseRepository.querySessionsWithoutRoom(ENGELSYSTEM_ROOM_NAME)

    fun loadLastEngelsystemShiftsHash() =
            sharedPreferencesRepository.getLastEngelsystemShiftsHash()

    fun updateLastEngelsystemShiftsHash(hash: Int) =
            sharedPreferencesRepository.setLastEngelsystemShiftsHash(hash)

    fun loadEngelsystemShiftsHash() =
            sessionsDatabaseRepository.querySessionsWithinRoom(ENGELSYSTEM_ROOM_NAME).hashCode()

    fun loadDateInfos() =
            readSessionsOrderedByDateUtc().toDateInfos()

    private fun updateSessions(sessions: List<SessionNetworkModel>) {
        val sessionsDatabaseModel = sessions.toSessionsDatabaseModel()
        val list = sessionsDatabaseModel.map { it.sessionId to it.toContentValues() }.toTypedArray() // TODO sessionId -> id?
        sessionsDatabaseRepository.upsertSessions(*list)
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
    fun deleteSessionAlarmNotificationId(notificationId: Int): Boolean {
        return (sessionsDatabaseRepository.deleteSessionIdByNotificationId(notificationId) > 0).onFailure {
            logging.e(javaClass.simpleName, "Failure deleting sessionId for notificationId = $notificationId")
        }
    }

    fun loadMeta(): MetaAppModel =
            readMeta().toMetaAppModel()

    private fun readMeta(): MetaDatabaseModel =
            metaDatabaseRepository.query()

    /**
     * Updates the [Meta] information in the database.
     *
     * The [Meta.eTag] field should only be written if a network response is received
     * with a status code of HTTP 200 (OK).
     *
     * See also: [HttpStatus.HTTP_OK]
     */
    private fun updateMeta(meta: MetaNetworkModel) {
        val metaDatabaseModel = meta.toMetaDatabaseModel()
        val values = metaDatabaseModel.toContentValues()
        metaDatabaseRepository.insert(values)
    }

    fun loadAlarmTimeIndex() =
            sharedPreferencesRepository.getAlarmTimeIndex()

    /**
     * Returns the alarm tone `Uri` or `null` for silent alarms to be used for notifications.
     */
    fun loadAlarmToneUri(): Uri? {
        val alarmTone = sharedPreferencesRepository.getAlarmTone()
        return AlarmToneConversion.getNotificationIntentUri(alarmTone, AlarmTonePreference.DEFAULT_VALUE_URI)
    }

    fun loadAlternativeHighlightingEnabled() =
            sharedPreferencesRepository.isAlternativeHighlightingEnabled()

    fun loadAutoUpdateEnabled() =
            sharedPreferencesRepository.isAutoUpdateEnabled()

    fun loadScheduleUrl(): String {
        val alternateScheduleUrl = sharedPreferencesRepository.getAlternativeScheduleUrl()
        return if (alternateScheduleUrl.isEmpty()) {
            BuildConfig.SCHEDULE_URL
        } else {
            alternateScheduleUrl
        }
    }

    private fun readEngelsystemShiftsUrl() =
            sharedPreferencesRepository.getEngelsystemShiftsUrl()

    fun loadScheduleLastFetchedAt() =
            sharedPreferencesRepository.getScheduleLastFetchedAt()

    private fun updateScheduleLastFetchedAt() = with(Moment.now()) {
        sharedPreferencesRepository.setScheduleLastFetchedAt(toMilliseconds())
    }

    fun loadScheduleChangesSeen() =
            sharedPreferencesRepository.getChangesSeen()

    fun updateScheduleChangesSeen(changesSeen: Boolean) =
            sharedPreferencesRepository.setChangesSeen(changesSeen)

    private fun resetChangesSeenFlag() =
            updateScheduleChangesSeen(false)

    fun loadDisplayDayIndex() =
            sharedPreferencesRepository.getDisplayDayIndex()

    fun updateDisplayDayIndex(displayDayIndex: Int) =
            sharedPreferencesRepository.setDisplayDayIndex(displayDayIndex)

    fun loadInsistentAlarmsEnabled() =
            sharedPreferencesRepository.isInsistentAlarmsEnabled()

    @Deprecated("Replace this with a push-based update mechanism")
    fun setOnSessionsChangeListener(onSessionsChangeListener: OnSessionsChangeListener) {
        this.onSessionsChangeListener?.let {
            logging.e(javaClass.simpleName, "Setting a new listener while there's already one active")
        }

        if (highlightsHaveChanged) {
            onSessionsChangeListener.onHighlightsChanged()
            highlightsHaveChanged = false
        }

        if (alarmsHaveChanged) {
            onSessionsChangeListener.onAlarmsChanged()
            alarmsHaveChanged = false
        }

        this.onSessionsChangeListener = onSessionsChangeListener
    }

    @Deprecated("Replace this with a push-based update mechanism")
    fun removeOnSessionsChangeListener(onSessionsChangeListener: OnSessionsChangeListener) {
        if (this.onSessionsChangeListener == onSessionsChangeListener) {
            this.onSessionsChangeListener = null
        } else {
            logging.e(javaClass.simpleName, "Asked to remove listener that wasn't the active listener")
        }
    }

    @Deprecated("Users of AppRepository should not have to be responsible for triggering change notifications. " +
            "Replace with a mechanism internal to AppRepository.")
    fun notifyHighlightsChanged() {
        onSessionsChangeListener.let { listener ->
            if (listener == null) {
                highlightsHaveChanged = true
            } else {
                listener.onHighlightsChanged()
            }
        }
    }

    @Deprecated("Users of AppRepository should not have to be responsible for triggering change notifications. " +
            "Replace with a mechanism internal to AppRepository.")
    fun notifyAlarmsChanged() {
        onSessionsChangeListener.let { listener ->
            if (listener == null) {
                alarmsHaveChanged = true
            } else {
                listener.onAlarmsChanged()
            }
        }
    }
}
