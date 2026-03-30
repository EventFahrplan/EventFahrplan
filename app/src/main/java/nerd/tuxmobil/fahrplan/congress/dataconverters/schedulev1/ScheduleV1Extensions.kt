@file:OptIn(ExperimentalUuidApi::class)

package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import info.metadude.android.eventfahrplan.network.serialization.ParserTask
import info.metadude.kotlin.library.schedule.v1.models.Conference
import info.metadude.kotlin.library.schedule.v1.models.ScheduleV1
import kotlin.uuid.ExperimentalUuidApi
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator as ScheduleGeneratorNetworkModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

fun ScheduleV1.toMetaNetworkModel(
    responseETag: String,
    responseLastModifiedAt: String,
) = MetaNetworkModel(
    scheduleGenerator = ScheduleGeneratorNetworkModel(
        name = generator?.name,
        version = generator?.version,
    ),
    httpHeader = HttpHeaderNetworkModel(
        eTag = responseETag,
        lastModified = responseLastModifiedAt,
    ),
    numDays = schedule.conference.daysCount,
    title = schedule.conference.title,
    timeZoneName = schedule.conference.timeZoneName?.id,
    version = schedule.version,
)

fun ScheduleV1.toSessionsNetworkModel(): List<SessionNetworkModel> {
    val conference = schedule.conference
    val roomIndexByRoomName = conference.toRoomIndexByRoomName()
    val roomGuidByRoomName = conference.rooms.associate { it.name to it.guid.toString() }
    return conference.days.flatMap { day ->
        day.rooms.flatMap { (roomName, events) ->
            val roomGuid = roomGuidByRoomName[roomName].orEmpty()
            val roomIndex = roomIndexByRoomName.getValue(roomName)
            events.map { event ->
                event.toSessionNetworkModel(
                    day = day,
                    roomGuid = roomGuid,
                    roomIndex = roomIndex,
                )
            }
        }
    }
}

/**
 * Assigns each distinct room name a stable index in first-seen order (day list order, then room map
 * key order), matching [ParserTask] XML parsing.
 */
fun Conference.toRoomIndexByRoomName(): Map<String, Int> {
    val indexByName = linkedMapOf<String, Int>()
    var next = 0
    for (day in days) {
        for (roomName in day.rooms.keys) {
            if (roomName !in indexByName) {
                indexByName[roomName] = next++
            }
        }
    }
    return indexByName
}
