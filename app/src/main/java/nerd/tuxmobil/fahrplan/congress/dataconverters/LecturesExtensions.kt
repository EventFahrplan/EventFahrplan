package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import info.metadude.android.eventfahrplan.database.models.Session as LectureDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as LectureNetworkModel

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

fun List<Lecture>.toDayRanges(): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val day = Moment(it.date)
        val dayRange = DayRange(day)
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

fun List<LectureNetworkModel>.toLecturesAppModel2(): List<Lecture> = map(LectureNetworkModel::toLectureAppModel)

fun List<LectureDatabaseModel>.toLecturesAppModel() = map(LectureDatabaseModel::toLectureAppModel)

fun List<Lecture>.sanitize(): List<Lecture> = map(Lecture::sanitize)
