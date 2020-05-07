package nerd.tuxmobil.fahrplan.congress.repositories

import android.support.v4.util.SparseArrayCompat
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData

class LectureListTransformer(private val prioritizedRoomProvider: PrioritizedRoomProvider) {

    /**
     * Transform data loaded from the database to a data structure that we would like to use in
     * UI code.
     * Then transform it to the (legacy) data structure that the UI is currently using.
     */
    fun legacyTransformLectureList(dayIndex: Int, lectures: List<Lecture>): LegacyLectureData {
        val scheduleData = transformLectureList(dayIndex, lectures)

        // Set Lecture.roomIndex to match the index in ScheduleData.roomDataList
        scheduleData.roomDataList.forEachIndexed { index, roomData ->
            roomData.lectures.forEach { it.roomIndex = index }
        }

        // Map room title to room index
        val roomsMap = scheduleData.roomDataList
                .mapIndexed { index, roomData -> roomData.roomName to index }
                .toMap()

        // Map column index to room index
        // This is simple identity map (0 -> 0, 1 -> 1, etc.)
        // Room indices provided by the database are discarded here on purpose.
        // Future UI code which assembles room columns should not be based on these room indices.
        val roomList = SparseArrayCompat<Int>()
        for (index in scheduleData.roomDataList.indices) {
            roomList.put(index, index)
        }

        return LegacyLectureData(
                lectureListDay = dayIndex,
                lectureList = lectures.sortedBy { it.dateUTC },
                roomsMap = roomsMap,
                roomList = roomList,
                roomCount = scheduleData.roomDataList.size,
                scheduleData = scheduleData
        )
    }

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

        return ScheduleData(dayIndex, roomDataList)
    }

}

interface PrioritizedRoomProvider {
    val prioritizedRooms: List<String>
}

@Deprecated("Use ScheduleData instead")
data class LegacyLectureData(
        val lectureListDay: Int,
        val lectureList: List<Lecture>,
        val roomsMap: Map<String, Int>,
        val roomList: SparseArrayCompat<Int>,
        val roomCount: Int,

        // Also include the new data structure so we can gradually switch over to it
        val scheduleData: ScheduleData
)
