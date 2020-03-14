package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.LecturesDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper
import info.metadude.android.eventfahrplan.engelsystem.EngelsystemNetworkRepository
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import info.metadude.kotlin.library.engelsystem.models.Shift
import kotlinx.coroutines.Job
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.dataconverters.*
import nerd.tuxmobil.fahrplan.congress.exceptions.AppExceptionHandler
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.*
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges
import okhttp3.OkHttpClient

object AppRepository {

    /**
     * Name used as the display title for the Engelsystem column and
     * for the database column. Do not change it!
     * Also used in app/src/<flavor>/res/xml/track_resource_names.xml.
     */
    const val ENGELSYSTEM_ROOM_NAME = "Engelshifts"
    const val ALL_DAYS = -1

    private lateinit var context: Context

    private lateinit var logging: Logging

    private val parentJobs = mutableMapOf<String, Job>()
    private lateinit var networkScope: NetworkScope

    private lateinit var alarmsDatabaseRepository: AlarmsDatabaseRepository
    private lateinit var highlightsDatabaseRepository: HighlightsDatabaseRepository
    private lateinit var lecturesDatabaseRepository: LecturesDatabaseRepository
    private lateinit var metaDatabaseRepository: MetaDatabaseRepository

    private lateinit var scheduleNetworkRepository: ScheduleNetworkRepository
    private lateinit var engelsystemNetworkRepository: EngelsystemNetworkRepository
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    @JvmOverloads
    fun initialize(
            context: Context,
            logging: Logging,
            networkScope: NetworkScope = NetworkScope.of(AppExecutionContext, AppExceptionHandler(logging)),
            alarmsDatabaseRepository: AlarmsDatabaseRepository = AlarmsDatabaseRepository(AlarmsDBOpenHelper(context)),
            highlightsDatabaseRepository: HighlightsDatabaseRepository = HighlightsDatabaseRepository(HighlightDBOpenHelper(context)),
            lecturesDatabaseRepository: LecturesDatabaseRepository = LecturesDatabaseRepository(LecturesDBOpenHelper(context), logging),
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
        this.lecturesDatabaseRepository = lecturesDatabaseRepository
        this.metaDatabaseRepository = metaDatabaseRepository
        this.scheduleNetworkRepository = scheduleNetworkRepository
        this.engelsystemNetworkRepository = engelsystemNetworkRepository
        this.sharedPreferencesRepository = sharedPreferencesRepository
    }

    private fun loadingFailed(@Suppress("SameParameterValue") requestIdentifier: String) {
        parentJobs.remove(requestIdentifier)
    }

    fun cancelLoading() {
        parentJobs.values.forEach(Job::cancel)
        parentJobs.clear()
    }

    fun loadSchedule(url: String,
                     eTag: String,
                     okHttpClient: OkHttpClient,
                     onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
                     onParsingDone: (parseScheduleResult: ParseResult) -> Unit,
                     onLoadingShiftsDone: (loadShiftsResult: LoadShiftsResult) -> Unit
    ) {
        check(onFetchingDone != {}) { "Nobody registered to receive FetchScheduleResult." }
        // Fetching
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, eTag) { fetchScheduleResult ->
            val fetchResult = fetchScheduleResult.toAppFetchScheduleResult()
            onFetchingDone.invoke(fetchResult)

            if (fetchResult.isSuccessful) {
                check(onParsingDone != {}) { "Nobody registered to receive ParseScheduleResult." }
                // Parsing
                parseSchedule(
                        fetchResult.scheduleXml,
                        fetchResult.eTag,
                        okHttpClient,
                        onParsingDone,
                        onLoadingShiftsDone
                )
            }
            if (HttpStatus.HTTP_NOT_MODIFIED == fetchResult.httpStatus) {
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
                onUpdateLectures = { lectures ->
                    val oldLectures = loadLecturesForAllDays(true)
                    val newLectures = lectures.toLecturesAppModel2().sanitize()
                    val hasChanged = ScheduleChanges.hasScheduleChanged(newLectures, oldLectures)
                    if (hasChanged) {
                        resetChangesSeenFlag()
                    }
                    updateLectures(newLectures)
                },
                onUpdateMeta = { meta ->
                    updateMeta(meta.toMetaAppModel())
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
    private fun updateShifts(shifts: List<Shift>) {
        if (shifts.isEmpty()) {
            return
        }
        val dayRanges = loadLecturesForAllDays(includeEngelsystemShifts = false)
                .toDayRanges()
        val lecturizedShifts = shifts
                .also { logging.d(javaClass.simpleName, "Shifts unfiltered = ${it.size}") }
                .cropToDayRangesExtent(dayRanges)
                .also { logging.d(javaClass.simpleName, "Shifts filtered = ${it.size}") }
                .toLectureAppModels(logging, ENGELSYSTEM_ROOM_NAME, dayRanges)
                .sanitize()
        val lectures = loadLecturesForAllDays(false) // Drop all shifts before ...
                .toMutableList()
                // Shift rooms to make space for the Engelshifts room
                .shiftRoomIndicesOfMainSchedule(lecturizedShifts.toDayIndices())
                .plus(lecturizedShifts) // ... adding them again.
                .toList()
        // TODO Detect shift changes as it happens for lectures
        updateLectures(lectures)
    }

    /**
     * Loads all lectures from the database which have been favored aka. starred but no canceled.
     * The returned list might be empty.
     */
    fun loadStarredLectures() = loadLecturesForAllDays(true)
            .filter { it.highlight && !it.changedIsCanceled }
            .also { logging.d(javaClass.simpleName, "${it.size} lectures starred.") }

    /**
     * Loads all lectures from the database which have been marked as changed, cancelled or new.
     * The returned list might be empty.
     */
    fun loadChangedLectures() = loadLecturesForAllDays(true)
            .filter { it.isChanged || it.changedIsCanceled || it.changedIsNew }
            .also { logging.d(javaClass.simpleName, "${it.size} lectures changed.") }

    /**
     * Loads all lectures from the database which take place on all days.
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    private fun loadLecturesForAllDays(includeEngelsystemShifts: Boolean) =
            loadLecturesForDayIndex(ALL_DAYS, includeEngelsystemShifts)

    /**
     * Loads all lectures from the database which take place on the specified [day][dayIndex].
     * All days can be loaded if -1 is passed as the [day][dayIndex].
     * To exclude Engelsystem shifts pass false to [includeEngelsystemShifts].
     */
    fun loadLecturesForDayIndex(dayIndex: Int, includeEngelsystemShifts: Boolean): List<Lecture> {
        val lectures = if (dayIndex == ALL_DAYS) {
            logging.d(javaClass.simpleName, "Loading lectures for all days.")
            if (includeEngelsystemShifts) {
                readLecturesOrderedByDateUtc()
            } else {
                readLecturesOrderedByDateUtcExcludingEngelsystemShifts()
            }
        } else {
            logging.d(javaClass.simpleName, "Loading lectures for day $dayIndex.")
            readLecturesForDayIndexOrderedByDateUtc(dayIndex)
        }
        logging.d(javaClass.simpleName, "Got ${lectures.size} rows.")

        val highlights = readHighlights()
        for (highlight in highlights) {
            logging.d(javaClass.simpleName, "$highlight")
            for (lecture in lectures) {
                if (lecture.lectureId == "${highlight.eventId}") {
                    lecture.highlight = highlight.isHighlight
                }
            }
        }
        return lectures.toList()
    }

    @JvmOverloads
    fun readAlarms(eventId: String = "") = if (eventId.isEmpty()) {
        alarmsDatabaseRepository.query().toAlarmsAppModel()
    } else {
        alarmsDatabaseRepository.query(eventId).toAlarmsAppModel()
    }

    fun deleteAlarmForAlarmId(alarmId: Int) =
            alarmsDatabaseRepository.deleteForAlarmId(alarmId)

    fun deleteAlarmForEventId(eventId: String) =
            alarmsDatabaseRepository.deleteForEventId(eventId)

    fun updateAlarm(alarm: Alarm) {
        val alarmDatabaseModel = alarm.toAlarmDatabaseModel()
        val values = alarmDatabaseModel.toContentValues()
        alarmsDatabaseRepository.update(values, alarm.eventId)
    }

    private fun readHighlights() =
            highlightsDatabaseRepository.query().toHighlightsAppModel()

    fun updateHighlight(lecture: Lecture) {
        val highlightDatabaseModel = lecture.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        highlightsDatabaseRepository.update(values, lecture.lectureId)
    }

    fun deleteAllHighlights() {
        highlightsDatabaseRepository.deleteAll()
    }

    fun readLectureByLectureId(lectureId: String): Lecture {
        val lecture = lecturesDatabaseRepository.queryLectureByLectureId(lectureId).toLectureAppModel()

        val highlight = highlightsDatabaseRepository.queryByEventId(lectureId.toInt())
        if (highlight != null) {
            lecture.highlight = highlight.isHighlight
        }

        val alarms = alarmsDatabaseRepository.query(lectureId)
        if (alarms.isNotEmpty()) {
            lecture.hasAlarm = true
        }

        return lecture
    }

    private fun readLecturesForDayIndexOrderedByDateUtc(dayIndex: Int) =
            lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex).toLecturesAppModel()

    private fun readLecturesOrderedByDateUtc() =
            lecturesDatabaseRepository.queryLecturesOrderedByDateUtc().toLecturesAppModel()

    private fun readLecturesOrderedByDateUtcExcludingEngelsystemShifts() =
            lecturesDatabaseRepository.queryLecturesWithoutRoom(ENGELSYSTEM_ROOM_NAME).toLecturesAppModel()

    fun readLastEngelsystemShiftsHash() =
            sharedPreferencesRepository.getLastEngelsystemShiftsHash()

    fun updateLastEngelsystemShiftsHash(hash: Int) =
            sharedPreferencesRepository.setLastEngelsystemShiftsHash(hash)

    fun readEngelsystemShiftsHash() =
            lecturesDatabaseRepository.queryLecturesWithinRoom(ENGELSYSTEM_ROOM_NAME).hashCode()

    fun readDateInfos() =
            readLecturesOrderedByDateUtc().toDateInfos()

    /**
     * Updates the legacy static lecture list.
     * This method should be invoked when the state of a [lecture] is changed
     * while other parts of the application such as the main screen render
     * their views based on the [legacy static lecture list][MyApp.lectureList].
     */
    @Deprecated("Drop method once MyApp.lectureList is gone.")
    fun updateLecturesLegacy(lecture: Lecture) {
        MyApp.lectureList?.firstOrNull { lecture.lectureId == it.lectureId }?.let {
            it.highlight = lecture.highlight
            it.hasAlarm = lecture.hasAlarm
        }
    }

    private fun updateLectures(lectures: List<Lecture>) {
        val lecturesDatabaseModel = lectures.toLecturesDatabaseModel()
        val list = lecturesDatabaseModel.map { it.toContentValues() }
        lecturesDatabaseRepository.insert(list)
    }

    fun readMeta() =
            metaDatabaseRepository.query().toMetaAppModel()

    private fun updateMeta(meta: Meta) {
        val metaDatabaseModel = meta.toMetaDatabaseModel()
        val values = metaDatabaseModel.toContentValues()
        metaDatabaseRepository.insert(values)
    }

    fun readScheduleUrl(): String {
        val alternateScheduleUrl = sharedPreferencesRepository.getScheduleUrl()
        return if (alternateScheduleUrl.isEmpty()) {
            BuildConfig.SCHEDULE_URL
        } else {
            alternateScheduleUrl
        }
    }

    fun updateScheduleUrl(url: String) = sharedPreferencesRepository.setScheduleUrl(url)

    private fun readEngelsystemShiftsUrl() =
            sharedPreferencesRepository.getEngelsystemShiftsUrl()

    fun updateEngelsystemShiftsUrl(url: String) = sharedPreferencesRepository.setEngelsystemShiftsUrl(url)

    fun readScheduleLastFetchingTime() =
            sharedPreferencesRepository.getScheduleLastFetchedAt()

    fun updateScheduleLastFetchingTime() = with(Moment()) {
        sharedPreferencesRepository.setScheduleLastFetchedAt(toMilliseconds())
    }

    fun sawScheduleChanges() =
            sharedPreferencesRepository.getChangesSeen()

    fun updateScheduleChangesSeen(changesSeen: Boolean) =
            sharedPreferencesRepository.setChangesSeen(changesSeen)

    private fun resetChangesSeenFlag() =
            updateScheduleChangesSeen(false)

}
