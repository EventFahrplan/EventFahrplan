package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.models.VirtualDay
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

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

/**
 * Splits the given sessions into [VirtualDay]s. The [Session.date] field is used to separate them.
 * This field is unique for a virtual day, even for sessions after midnight.
 */
fun List<Session>.toVirtualDays(): List<VirtualDay> {
    var index = 0
    return groupBy { it.date }
        .map { (_, sessions) ->
            val sorted = sessions.sortedBy { it.dateUTC }
            VirtualDay(++index, sorted)
        }
}

fun List<Session>.toDateInfos() = map(Session::toDateInfo)

fun List<Session>.toSessionsDatabaseModel() = map(Session::toSessionDatabaseModel)

fun List<Session>.toDayRanges(): List<DayRange> {
    val ranges = mutableSetOf<DayRange>()
    forEach {
        val day = Moment.parseDate(it.date)
        val dayRange = DayRange(day)
        ranges.add(dayRange)
    }
    return ranges.sortedBy { it.startsAt }.toList()
}

fun List<SessionNetworkModel>.toSessionsAppModel2(): List<Session> = map(SessionNetworkModel::toSessionAppModel)

fun List<SessionDatabaseModel>.toSessionsAppModel() = map(SessionDatabaseModel::toSessionAppModel)

fun List<Session>.sanitize(): List<Session> = map(Session::sanitize)
