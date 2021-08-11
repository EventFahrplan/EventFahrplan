package nerd.tuxmobil.fahrplan.congress.repositories

import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionsTransformer(private val prioritizedRoomProvider: PrioritizedRoomProvider) {

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
        val roomMap = prioritizedRoomProvider.prioritizedRooms
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
        }

        return ScheduleData(dayIndex, roomDataList)
    }
}

interface PrioritizedRoomProvider {
    val prioritizedRooms: List<String>
}
