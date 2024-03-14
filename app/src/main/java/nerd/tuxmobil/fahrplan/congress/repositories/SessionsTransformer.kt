package nerd.tuxmobil.fahrplan.congress.repositories

import androidx.annotation.VisibleForTesting
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionsTransformer @VisibleForTesting constructor(

    private val roomProvider: RoomProvider

) {

    companion object {

        fun createSessionsTransformer(
            // A potential custom room name is applied at a later stage.
            defaultEngelsystemRoomName: String = AppRepository.ENGELSYSTEM_ROOM_NAME
        ): SessionsTransformer {
            val roomProvider = object : RoomProvider {
                override val prioritizedRooms: List<String> = listOf(
                    defaultEngelsystemRoomName,
                    "Saal 1",
                    "Saal Grace",
                    "Saal Zuse",
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
            .associateWith { mutableListOf<Session>() }
            .toMutableMap()

        val sortedSessions = sessions.sortedBy { it.roomIndex }
        for (session in sortedSessions) {
            val sessionsInRoom = roomMap.getOrPut(session.roomName) { mutableListOf() }
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
    val (tail, head) = partition { it.roomName in deprioritizedRooms }
    val sortedTail = deprioritizedRooms.mapNotNull { roomName ->
        tail.firstOrNull { roomName == it.roomName }
    }
    return head + sortedTail
}
