package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.annotation.ColorRes
import androidx.compose.ui.unit.Dp

data class SessionCardData(
    val sessionId: String,
    val title: SessionProperty<String>,
    val subtitle: SessionProperty<String>? = null,
    val speakerNames: SessionProperty<String>? = null,
    val languages: SessionProperty<String>? = null,
    val trackName: SessionProperty<String>? = null,
    val recordingOptOut: SessionProperty<Boolean>? = null,
    val stateContentDescription: String,
    val innerHorizontalPadding: Float,
    val innerVerticalPadding: Float,
    val cardHeight: Dp,
    val isFavored: Boolean,
    val hasAlarm: Boolean,
    val showBorder: Boolean,
    val shouldShowShareSubMenu: Boolean,
    @param:ColorRes val backgroundColor: Int,
    @param:ColorRes val textColor: Int,
)
