package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session
import info.metadude.android.eventfahrplan.database.models.Session as LectureDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as LectureNetworkModel

fun List<Session>.shiftRoomIndicesOfMainSchedule(dayIndices: Set<Int>) = map {
    it.shiftRoomIndexOnDays(dayIndices)
}

fun List<Session>.toDayIndices(): Set<Int> {
    val dayIndices = HashSet<Int>()
    forEach {
        dayIndices.add(it.day)
    }
    return dayIndices
}

fun List<Session>.toDateInfos() = map(Session::toDateInfo)

fun List<Session>.toLecturesDatabaseModel() = map(Session::toLectureDatabaseModel)

fun List<Session>.toDayRanges(): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val day = Moment(it.date)
        val dayRange = DayRange(day)
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

fun List<LectureNetworkModel>.toLecturesAppModel2(): List<Session> = map(LectureNetworkModel::toLectureAppModel)

fun List<LectureDatabaseModel>.toLecturesAppModel() = map(LectureDatabaseModel::toLectureAppModel)

fun List<Session>.sanitize(): List<Session> = map(Session::sanitize)
