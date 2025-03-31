package nerd.tuxmobil.fahrplan.congress.schedule

data class RoomColumnData(
    val sessionData: List<SessionCardData>,
    val spacings: List</* sessionIndex */ Int>, // Spacings between sessions in dp
)
