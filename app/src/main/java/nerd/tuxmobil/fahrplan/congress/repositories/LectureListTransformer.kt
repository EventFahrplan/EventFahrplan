package nerd.tuxmobil.fahrplan.congress.repositories

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData

class LectureListTransformer(private val prioritizedRoomProvider: PrioritizedRoomProvider) {

    /**
     * Transforms the given [lectures] for the given [dayIndex] into a [ScheduleData] object.
     *
     * Apart from the [dayIndex] it contains a list of room names and their associated lectures
     * (sorted by [Lecture.dateUTC]). Rooms names are added in a defined order: room names of
     * prioritized rooms first, then all other room names in the order defined by the given lecture.
     * After adding room names the original order [Lecture.roomIndex] is no longer of interest.
     */
    fun transformLectureList(dayIndex: Int, lectures: List<Lecture>): ScheduleData {
        // Pre-populate the map with prioritized rooms
        val roomMap = prioritizedRoomProvider.prioritizedRooms
                .map { it to mutableListOf<Lecture>() }
                .toMap()
                .toMutableMap()

        val sortedLectures = lectures.sortedBy { it.roomIndex }
        for (lecture in sortedLectures) {
            val lecturesInRoom = roomMap.getOrPut(lecture.room) { mutableListOf() }
            lecturesInRoom.add(lecture)
        }

        val roomDataList = roomMap.mapNotNull { (roomName, lectures) ->
            if (lectures.isEmpty()) {
                // Drop prioritized rooms without lectures
                null
            } else {
                RoomData(
                        roomName = roomName,
                        lectures = lectures.sortedBy { it.dateUTC }.toList()
                )
            }
        }

        legacyRoomIndexFixup(roomDataList)

        return ScheduleData(dayIndex, roomDataList)
    }

    private fun legacyRoomIndexFixup(roomDataList: List<RoomData>) {
        // Set Lecture.roomIndex to match the index in ScheduleData.roomDataList
        roomDataList.forEachIndexed { index, roomData ->
            roomData.lectures.forEach { it.roomIndex = index }
        }
    }

}

interface PrioritizedRoomProvider {
    val prioritizedRooms: List<String>
}
