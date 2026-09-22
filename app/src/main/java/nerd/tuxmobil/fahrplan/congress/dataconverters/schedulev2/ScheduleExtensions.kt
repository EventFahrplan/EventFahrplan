package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.kotlin.library.schedule.v2.models.Conference
import info.metadude.kotlin.library.schedule.v2.models.Day
import info.metadude.kotlin.library.schedule.v2.models.Event
import info.metadude.kotlin.library.schedule.v2.models.EventState.CONFIRMED
import info.metadude.kotlin.library.schedule.v2.models.EventState.UNKNOWN
import info.metadude.kotlin.library.schedule.v2.models.Room
import info.metadude.kotlin.library.schedule.v2.models.Schedule
import info.metadude.kotlin.library.schedule.v2.models.ScheduledEvent
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

private const val LOG_TAG = "Schedule"

internal fun Schedule.toSessionsNetworkModel(
    logging: Logging,
    toSessionNetworkModel: Event.(
        day: Day,
        room: Room,
        roomIndex: Int,
        trackName: String,
    ) -> SessionNetworkModel = Event::toSessionNetworkModel,
): List<SessionNetworkModel> {
    val roomIndexByRoom = conference.toRoomIndexByRoom()
    val roomByGuid = roomIndexByRoom.keys.associateBy { it.guid }
    val days = conference.days
    val nameBySlug = conference.tracks.associate { it.slug.sanitize().lowercase() to it.name.sanitize() }

    days.forEach {
        logging.d(LOG_TAG, "Day ${it.index}: ${it.start} - ${it.end}")
    }

    return events
        .filterIsInstance<ScheduledEvent>()
        .filterEventsTakingPlace(logging)
        .filterRoomNotNull(logging)
        .mapNotNull { event ->
            val associatedRoom = roomByGuid[event.room?.guid]
            if (associatedRoom == null) {
                logging.e(LOG_TAG, "Unknown room: ${event.room} in ${event.guid}")
                return@mapNotNull null
            }
            val eventStartsAt = Moment.ofEpochMilli(event.start.toInstant().toEpochMilli())
            val associatedDay = days.firstOrNull { it.contains(eventStartsAt) }
            if (associatedDay == null) {
                logging.e(LOG_TAG, "Unknown day for event: start=${event.start}, guid=${event.guid}")
                return@mapNotNull null
            }
            event.toSessionNetworkModel(
                associatedDay,
                associatedRoom,
                roomIndexByRoom.getValue(associatedRoom),
                nameBySlug[event.track?.slug.sanitize().lowercase()].orEmpty(),
            )
        }
}

/**
 * Assigns each distinct room a stable index in first-seen order.
 */
private fun Conference.toRoomIndexByRoom(): Map<Room, Int> {
    val indexByRoom = linkedMapOf<Room, Int>()
    var next = 0
    for (room in rooms) {
        if (indexByRoom.keys.find { it.guid == room.guid } == null) {
            indexByRoom[room] = next++
        }
    }
    return indexByRoom
}

private fun List<ScheduledEvent>.filterEventsTakingPlace(logging: Logging) = filter {
    val include = it.state in setOf(CONFIRMED, UNKNOWN)
    if (!include) {
        logging.d(LOG_TAG, "Excluding event with state ${it.state}: ${it.guid}")
    }
    include
}

private fun List<ScheduledEvent>.filterRoomNotNull(logging: Logging) = filter {
    val include = it.room?.guid != null
    if (!include) {
        logging.d(LOG_TAG, "Excluding event with null room: ${it.guid}")
    }
    include
}

private fun Day.contains(startsAt: Moment): Boolean {
    val dayStartsAt = Moment.ofEpochMilli(start.toInstant().toEpochMilli())
    val dayEndsAt = Moment.ofEpochMilli(end.toInstant().toEpochMilli())
    val startsAtOrAfterDayStarts = startsAt == dayStartsAt || startsAt.isAfter(dayStartsAt)
    val startsBeforeDayEnds = startsAt.isBefore(dayEndsAt)
    return startsAtOrAfterDayStarts && startsBeforeDayEnds
}
