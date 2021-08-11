package nerd.tuxmobil.fahrplan.congress.repositories

import androidx.annotation.VisibleForTesting
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionsTransformer @VisibleForTesting constructor(

    private val roomProvider: RoomProvider

) {

    companion object {

        @JvmStatic
        fun createSessionsTransformer(): SessionsTransformer {
            val roomProvider = object : RoomProvider {
                override val prioritizedRooms: List<String> = listOf(
                    "Saal 1",
                    "Saal 2",
                    "Saal G",
                    "Saal 6",
                    "Saal 17",
                    "Lounge"
                )
                override val deprioritizedRooms: List<String> = emptyList()
            }
            return SessionsTransformer(roomProvider)
        }

    }

    /**
     * Transforms the given [sessions] for the given [dayIndex] into a [ScheduleData] object.
     *
     * Apart from the [dayIndex] it contains a list of room names and their associated sessions
     * (sorted by [Session.dateUTC]). Rooms names are added in a defined order: room names of
     * prioritized rooms first, then all other room names in the order defined by the given session.
     * After adding room names the original order [Session.roomIndex] is no longer of interest.
     */
    fun transformSessions(dayIndex: Int, sessions: List<Session>): ScheduleData {
        // Pre-populate the map with prioritized rooms
        val roomMap = roomProvider.prioritizedRooms
            .map { it to mutableListOf<Session>() }
            .toMap()
            .toMutableMap()

        val sortedSessions = sessions.sortedBy { it.roomIndex }
        for (session in sortedSessions) {
            val sessionsInRoom = roomMap.getOrPut(session.room) { mutableListOf() }
            sessionsInRoom.add(session)
        }

        val roomDataList = roomMap.mapNotNull { (roomName, sessions) ->
            if (sessions.isEmpty()) {
                // Drop prioritized rooms without sessions
                null
            } else {
                RoomData(
                    roomName = roomName,
                    sessions = sessions.sortedBy { it.dateUTC }.toList()
                )
            }
        }.sortWithDeprioritizedRooms(roomProvider.deprioritizedRooms)

        return ScheduleData(dayIndex, roomDataList)
    }
}

interface RoomProvider {
    val prioritizedRooms: List<String>
    val deprioritizedRooms: List<String>
}

/**
 * Moves all [RoomData] items with a room name contained in [deprioritizedRooms] to the end of the list.
 * The order of room names in the [deprioritizedRooms] list is applied to the receiving list.
 */
private fun List<RoomData>.sortWithDeprioritizedRooms(deprioritizedRooms: List<String>): List<RoomData> {
    if (deprioritizedRooms.isEmpty()) {
        return this
    }
    val (tail, head) = partition { deprioritizedRooms.contains(it.roomName) }
    val sortedTail = deprioritizedRooms.mapNotNull { room ->
        tail.firstOrNull { room == it.roomName }
    }
    return head + sortedTail
}
