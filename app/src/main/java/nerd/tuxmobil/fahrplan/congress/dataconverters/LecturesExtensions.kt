package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.DayRange
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import info.metadude.android.eventfahrplan.database.models.Lecture as LectureDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Lecture as LectureNetworkModel

fun List<Lecture>.shiftRoomIndicesOfMainSchedule(dayIndices: Set<Int>) = map {
    it.shiftRoomIndexOnDays(dayIndices)
}

fun List<Lecture>.toDayIndices(): Set<Int> {
    val dayIndices = HashSet<Int>()
    forEach {
        dayIndices.add(it.day)
    }
    return dayIndices
}

fun List<Lecture>.toDateInfos() = map(Lecture::toDateInfo)

fun List<Lecture>.toLecturesDatabaseModel() = map(Lecture::toLectureDatabaseModel)

fun List<Lecture>.toDayRanges(timeZoneOffset: ZoneOffset): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val dayRange = DayRange(it.getDayStartsAt(timeZoneOffset), it.getDayEndsAt(timeZoneOffset))
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

private fun Lecture.getDayStartsAt(timeZoneOffset: ZoneOffset): ZonedDateTime {
    val localDate = DateHelper.getLocalDate(date, "yyyy-MM-dd")
    return DateHelper.getDayStartsAtDate(localDate, timeZoneOffset)
}

private fun Lecture.getDayEndsAt(timeZoneOffset: ZoneOffset): ZonedDateTime {
    return DateHelper.getDayEndsAtDate(getDayStartsAt(timeZoneOffset))
}

fun List<LectureNetworkModel>.toLecturesAppModel2(): List<Lecture> = map(LectureNetworkModel::toLectureAppModel)

fun List<LectureDatabaseModel>.toLecturesAppModel() = map(LectureDatabaseModel::toLectureAppModel)

fun List<Lecture>.sanitize(): List<Lecture> = map(Lecture::sanitize)
