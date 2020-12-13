package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun List<SessionNetworkModel>.shiftRoomIndicesOfMainSchedule(dayIndices: Set<Int>) = map {
    it.shiftRoomIndexOnDays(dayIndices)
}

fun List<SessionNetworkModel>.toDayIndices(): Set<Int> {
    val dayIndices = HashSet<Int>()
    forEach {
        dayIndices.add(it.dayIndex)
    }
    return dayIndices
}

fun List<SessionDatabaseModel>.toDateInfos() = map(SessionDatabaseModel::toDateInfo)

fun List<SessionNetworkModel>.toDayRanges(): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val day = Moment.parseDate(it.date)
        val dayRange = DayRange(day)
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

fun List<SessionDatabaseModel>.toSessionsAppModel() = map(SessionDatabaseModel::toSessionAppModel)

fun List<SessionDatabaseModel>.toSessionsNetworkModel() = map(SessionDatabaseModel::toSessionNetworkModel)

fun List<SessionNetworkModel>.toSessionsDatabaseModel() = map(SessionNetworkModel::toSessionDatabaseModel)

fun List<SessionNetworkModel>.sanitize(): List<SessionNetworkModel> = map(SessionNetworkModel::sanitize)
