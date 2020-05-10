package nerd.tuxmobil.fahrplan.congress.models

data class ScheduleData(
        val dayIndex: Int,
        val roomDataList: List<RoomData>
) {
    val roomCount: Int
        get() = roomDataList.size

    val roomNames: List<String>
        get() = roomDataList.map { it.roomName }
}
