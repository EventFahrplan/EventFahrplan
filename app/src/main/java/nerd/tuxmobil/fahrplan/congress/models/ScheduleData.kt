package nerd.tuxmobil.fahrplan.congress.models

data class ScheduleData(
        val dayIndex: Int,
        val roomDataList: List<RoomData>
) {
    val roomCount: Int
        get() = roomDataList.size

    val roomNames: List<String>
        get() = roomDataList.map { it.roomName }

    val allLectures: List<Lecture>
        get() = roomDataList.flatMap { it.lectures }.sortedBy { it.dateUTC }

    fun findLecture(lectureId: String): Lecture? {
        return roomDataList.asSequence()
                .flatMap { it.lectures.asSequence() }
                .firstOrNull { it.lectureId == lectureId }
    }
}
