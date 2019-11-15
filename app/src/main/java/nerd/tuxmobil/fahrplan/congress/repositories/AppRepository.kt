package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import android.net.Uri
import android.text.format.Time
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.LecturesDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.dataconverters.*
import nerd.tuxmobil.fahrplan.congress.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges
import okhttp3.OkHttpClient
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

class AppRepository private constructor(val context: Context) {

    companion object : SingletonHolder<AppRepository, Context>(::AppRepository) {

        const val ALL_DAYS = -1

    }

    private val logging = Logging.get()

    private val alarmsDBOpenHelper = AlarmsDBOpenHelper(context)
    private val alarmsDatabaseRepository = AlarmsDatabaseRepository(alarmsDBOpenHelper)

    private val highlightDBOpenHelper = HighlightDBOpenHelper(context)
    private val highlightsDatabaseRepository = HighlightsDatabaseRepository(highlightDBOpenHelper)

    private val lecturesDBOpenHelper = LecturesDBOpenHelper(context)
    private val lecturesDatabaseRepository = LecturesDatabaseRepository(lecturesDBOpenHelper)

    private val metaDBOpenHelper = MetaDBOpenHelper(context)
    private val metaDatabaseRepository = MetaDatabaseRepository(metaDBOpenHelper)

    private val scheduleNetworkRepository = ScheduleNetworkRepository()

    private val sharedPreferencesRepository = SharedPreferencesRepository(context)

    fun loadSchedule(url: String,
                     eTag: String,
                     onFetchingDone: (fetchScheduleResult: FetchScheduleResult) -> Unit,
                     onParsingDone: (parseScheduleResult: ParseScheduleResult) -> Unit
    ) {

        val scheduleUrl = readScheduleUrl()
        val hostName = Uri.parse(scheduleUrl).host ?: throw NullPointerException("Host not present for URL: $scheduleUrl")
        val okHttpClient: OkHttpClient?
        try {
            okHttpClient = CustomHttpClient.createHttpClient(hostName)
        } catch (e: KeyManagementException) {
            onFetchingDone(FetchScheduleResult(httpStatus = HttpStatus.HTTP_SSL_SETUP_FAILURE, hostName = hostName))
            return
        } catch (e: NoSuchAlgorithmException) {
            onFetchingDone(FetchScheduleResult(httpStatus = HttpStatus.HTTP_SSL_SETUP_FAILURE, hostName = hostName))
            return
        }

        check(onFetchingDone != {}) { "Nobody registered to receive FetchScheduleResult." }
        // Fetching
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, eTag) { fetchScheduleResult ->
            val fetchResult = fetchScheduleResult.toAppFetchScheduleResult()
            onFetchingDone.invoke(fetchResult)

            if (fetchResult.isSuccessful) {
                check(onParsingDone != {}) { "Nobody registered to receive ParseScheduleResult." }
                // Parsing
                scheduleNetworkRepository.parseSchedule(fetchResult.scheduleXml, fetchResult.eTag,
                        onUpdateLectures = { lectures ->
                            val oldLectures = loadLecturesForAllDays()
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
                        })
            }
        }
    }

    /**
     * Loads all lectures from the database which take place on all days.
     */
    fun loadLecturesForAllDays() =
            loadLecturesForDayIndex(ALL_DAYS)

    /**
     * Loads all lectures from the database which take place on the specified [day][dayIndex].
     * All days can be loaded if -1 is passed as the [day][dayIndex].
     */
    fun loadLecturesForDayIndex(dayIndex: Int): List<Lecture> {
        val lectures = if (dayIndex == ALL_DAYS) {
            logging.d(javaClass.name, "Loading lectures for all days.")
            readLecturesOrderedByDateUtc()
        } else {
            logging.d(javaClass.name, "Loading lectures for day $dayIndex.")
            readLecturesForDayIndexOrderedByDateUtc(dayIndex)
        }
        logging.d(javaClass.name, "Got ${lectures.size} rows.")

        val highlights = readHighlights()
        for (highlight in highlights) {
            logging.d(javaClass.name, "$highlight")
            for (lecture in lectures) {
                if (lecture.lectureId == "" + highlight.eventId) {
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

    @JvmOverloads
    fun deleteAlarmForAlarmId(alarmId: Int, closeSQLiteOpenHelper: Boolean = true) =
            alarmsDatabaseRepository.deleteForAlarmId(alarmId, closeSQLiteOpenHelper)

    fun deleteAlarmForEventId(eventId: String) =
            alarmsDatabaseRepository.deleteForEventId(eventId)

    fun updateAlarm(alarm: Alarm) {
        val alarmDatabaseModel = alarm.toAlarmDatabaseModel()
        val values = alarmDatabaseModel.toContentValues()
        alarmsDatabaseRepository.insert(values, alarm.eventId)
    }

    private fun readHighlights() =
            highlightsDatabaseRepository.query().toHighlightsAppModel()

    fun updateHighlight(lecture: Lecture) {
        val highlightDatabaseModel = lecture.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        highlightsDatabaseRepository.insert(values, lecture.lectureId)
    }

    fun readLectureByLectureId(lectureId: String) =
            lecturesDatabaseRepository.queryLectureByLectureId(lectureId).first().toLectureAppModel()

    private fun readLecturesForDayIndexOrderedByDateUtc(dayIndex: Int) =
            lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex).toLecturesAppModel()

    private fun readLecturesOrderedByDateUtc() =
            lecturesDatabaseRepository.queryLecturesOrderedByDateUtc().toLecturesAppModel()

    fun readDateInfos() =
            readLecturesOrderedByDateUtc().toDateInfos()

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

    fun readScheduleLastFetchingTime() =
            sharedPreferencesRepository.getScheduleLastFetchedAt()

    fun updateScheduleLastFetchingTime() = with(Time()) {
        setToNow()
        sharedPreferencesRepository.setScheduleLastFetchedAt(toMillis(true))
    }

    fun sawScheduleChanges() =
            sharedPreferencesRepository.getChangesSeen()

    fun updateScheduleChangesSeen(changesSeen: Boolean) =
            sharedPreferencesRepository.setChangesSeen(changesSeen)

    private fun resetChangesSeenFlag() =
            updateScheduleChangesSeen(false)

}
