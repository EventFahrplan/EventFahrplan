package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.VirtualDay
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

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

/**
 * Splits the given sessions into [VirtualDay]s. The [SessionAppModel.dateText] field is used to separate them.
 * This field is unique for a virtual day, even for sessions after midnight.
 */
fun List<SessionAppModel>.toVirtualDays(): List<VirtualDay> {
    var index = 0
    return groupBy { it.dateText }
        .map { (_, sessions) ->
            val sorted = sessions.sortedBy { it.dateUTC }
            VirtualDay(++index, sorted)
        }
}

fun List<SessionAppModel>.toNumDays(): Int {
    return map(SessionAppModel::dateText).distinct().size
}

fun List<SessionDatabaseModel>.toDateInfos() = map(SessionDatabaseModel::toDateInfo)

fun List<SessionDatabaseModel>.toDayRanges(): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val day = Moment.parseDate(it.dateText)
        val dayRange = DayRange(day)
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

fun List<SessionDatabaseModel>.toSessionsNetworkModel() = map(SessionDatabaseModel::toSessionNetworkModel)

fun List<SessionNetworkModel>.toSessionsDatabaseModel() = map(SessionNetworkModel::toSessionDatabaseModel)

fun List<SessionDatabaseModel>.toSessionsAppModel() = map(SessionDatabaseModel::toSessionAppModel)

fun List<SessionNetworkModel>.sanitize(): List<SessionNetworkModel> = map(SessionNetworkModel::sanitize)
