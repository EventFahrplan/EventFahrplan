package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import android.net.Uri
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
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import okhttp3.OkHttpClient
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

class AppRepository private constructor(val context: Context) {

    companion object : SingletonHolder<AppRepository, Context>(::AppRepository)

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
                     onParsingDone: (result: Boolean, version: String) -> Unit
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

        // Fetching
        scheduleNetworkRepository.fetchSchedule(okHttpClient, url, eTag) { fetchScheduleResult ->
            onFetchingDone.invoke(fetchScheduleResult.toAppFetchScheduleResult())

            if (fetchScheduleResult.isSuccessful()) {
                // Parsing
                scheduleNetworkRepository.parseSchedule(fetchScheduleResult.scheduleXml, fetchScheduleResult.eTag,
                        onUpdateLectures = { lectures ->
                            val oldLectures = FahrplanMisc.loadLecturesForAllDays(this)
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
                            onParsingDone(result, version)
                        })
            }
        }
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

    fun readHighlights() =
            highlightsDatabaseRepository.query().toHighlightsAppModel()

    fun updateHighlight(lecture: Lecture) {
        val highlightDatabaseModel = lecture.toHighlightDatabaseModel()
        val values = highlightDatabaseModel.toContentValues()
        highlightsDatabaseRepository.insert(values, lecture.lectureId)
    }

    fun readLectureByLectureId(lectureId: String) =
            lecturesDatabaseRepository.queryLectureByLectureId(lectureId).first().toLectureAppModel()

    fun readLecturesForDayIndexOrderedByDateUtc(dayIndex: Int) =
            lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex).toLecturesAppModel()

    fun readLecturesOrderedByDateUtc() =
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

    private fun resetChangesSeenFlag() =
            sharedPreferencesRepository.setChangesSeen(false)

}
