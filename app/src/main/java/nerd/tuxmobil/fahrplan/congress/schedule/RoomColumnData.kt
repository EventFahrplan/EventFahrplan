package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.compose.ui.unit.Dp

data class RoomColumnData(
    val sessionData: List<SessionCardData>,
    val spacings: List</* sessionIndex */ Dp>, // Spacings between sessions in dp
)
