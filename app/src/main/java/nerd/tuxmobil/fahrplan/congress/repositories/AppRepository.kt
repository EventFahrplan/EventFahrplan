package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.Context
import info.metadude.android.eventfahrplan.database.extensions.toContentValues
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.LecturesDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.HighlightDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.MetaDBOpenHelper
import nerd.tuxmobil.fahrplan.congress.dataconverters.*
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository

class AppRepository private constructor(context: Context) {

    companion object : SingletonHolder<AppRepository, Context>(::AppRepository)

    private val alarmsDBOpenHelper = AlarmsDBOpenHelper(context)
    private val alarmsDatabaseRepository = AlarmsDatabaseRepository(alarmsDBOpenHelper)

    private val highlightDBOpenHelper = HighlightDBOpenHelper(context)
    private val highlightsDatabaseRepository = HighlightsDatabaseRepository(highlightDBOpenHelper)

    private val lecturesDBOpenHelper = LecturesDBOpenHelper(context)
    private val lecturesDatabaseRepository = LecturesDatabaseRepository(lecturesDBOpenHelper)

    private val metaDBOpenHelper = MetaDBOpenHelper(context)
    private val metaDatabaseRepository = MetaDatabaseRepository(metaDBOpenHelper)

    private val sharedPreferencesRepository = SharedPreferencesRepository(context)

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
        highlightsDatabaseRepository.insert(values, lecture.lecture_id)
    }

    fun readLecturesForDayIndexOrderedByDateUtc(dayIndex: Int) =
            lecturesDatabaseRepository.queryLecturesForDayIndexOrderedByDateUtc(dayIndex).toLecturesAppModel()

    fun readLecturesOrderedByDate() =
            lecturesDatabaseRepository.queryLecturesOrderedByDate().toLecturesAppModel()

    fun readLecturesOrderedByDateUtc() =
            lecturesDatabaseRepository.queryLecturesOrderedByDateUtc().toLecturesAppModel()

    fun readDateInfos() =
            readLecturesOrderedByDateUtc().toDateInfos()

    fun updateLectures(lectures: List<Lecture>) {
        val lecturesDatabaseModel = lectures.toLecturesDatabaseModel()
        val list = lecturesDatabaseModel.map { it.toContentValues() }
        lecturesDatabaseRepository.insert(list)
    }

    fun readMeta() =
            metaDatabaseRepository.query().toMetaAppModel()

    fun updateMeta(meta: Meta) {
        val metaDatabaseModel = meta.toMetaDatabaseModel()
        val values = metaDatabaseModel.toContentValues()
        metaDatabaseRepository.insert(values)
    }

    fun resetChangesSeenFlag() =
            sharedPreferencesRepository.setChangesSeen(false)

}
