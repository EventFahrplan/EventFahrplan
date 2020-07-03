package nerd.tuxmobil.fahrplan.congress.models

/**
 * Represents schedule data of one or multiple rooms for a single day specified by its [dayIndex].
 * Please pass rooms in [roomDataList] in the order in which they should be displayed.
 */
data class ScheduleData(
        val dayIndex: Int,
        val roomDataList: List<RoomData>
) {

    companion object {
        const val UNKNOWN_ROOM_INDEX = -1
    }

    /**
     * Returns the total number of rooms.
     */
    val roomCount: Int
        get() = roomDataList.size

    /**
     * Returns list of room names sorted as passed via [ScheduleData.roomDataList].
     * The list might be empty.
     */
    val roomNames: List<String>
        get() = roomDataList.map { it.roomName }

    /**
     * Returns a list of all lectures sorted by [Session.dateUTC] ascending.
     * The list might be empty.
     */
    val allLectures: List<Session>
        get() = roomDataList.flatMap { it.lectures }.sortedBy { it.dateUTC }

    /**
     * Returns the first [Session] found which matches the given [lectureId] or `null` if not found.
     */
    fun findLecture(lectureId: String): Session? {
        return roomDataList.asSequence()
                .flatMap { it.lectures.asSequence() }
                .firstOrNull { it.lectureId == lectureId }
    }

    /**
     * Returns the room index of the given [lecture] or [UNKNOWN_ROOM_INDEX] if not found.
     */
    fun findRoomIndex(lecture: Session): Int {
        return roomDataList.indexOfFirst { it.roomName == lecture.room }
    }
}
